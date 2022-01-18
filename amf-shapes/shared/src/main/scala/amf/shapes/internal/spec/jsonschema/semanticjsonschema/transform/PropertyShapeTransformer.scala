package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{PropertyLikeMapping, PropertyMapping}
import amf.aml.internal.metamodel.domain.PropertyMappingModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.{DataType, ValueField}
import amf.core.client.scala.model.DataType._
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape}
import org.mulesoft.common.collections.FilterType

case class PropertyShapeTransformer(property: PropertyShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler) {

  val propertyMapping: PropertyMapping = PropertyMapping(property.annotations)

  def transform(): PropertyMapping = {

    setMappingName()
    setMappingID()

    property.range match {
      case scalar: ScalarShape => transformScalarProperty(scalar)
      case obj: NodeShape      => transformObjectProperty(obj)
      case array: ArrayShape   => transformArray(array)
      case any: AnyShape       => transformAnyProperty(any)
    }
    checkMandatoriness()
    checkSemantics()
    transformEnum(property, propertyMapping)
    propertyMapping
  }

  private def transformArray(array: ArrayShape) = {
    propertyMapping.withAllowMultiple(true)
    array.items match {
      case scalar: ScalarShape => transformScalarProperty(scalar)
      case obj: NodeShape      => transformObjectProperty(obj)
    }
    Option(array.default).foreach(propertyMapping.withDefault)
  }

  private def checkDefault(): Unit = Option(property.default).foreach(propertyMapping.withDefault)

  private def setMappingName(): Unit = propertyMapping.withName(property.name.value().replaceAll(" ", ""))

  private def setMappingID(): Unit = propertyMapping.withId(property.id)

  private def transformScalarProperty(scalar: ScalarShape): Unit = {
    // datatype
    val scalarRangeDatatype = sanitizeScalarRange(scalar.dataType.value())
    propertyMapping.withLiteralRange(scalarRangeDatatype)
    // pattern
    setWhenPresent(scalar.pattern, propertyMapping.withPattern)
    setWhenPresent(scalar.minimum, propertyMapping.withMinimum)
    setWhenPresent(scalar.maximum, propertyMapping.withMaximum)
    Option(scalar.default).foreach(propertyMapping.withDefault)
  }

  private def transformObjectProperty(obj: NodeShape): Unit = {
    val range = ShapeTransformation(obj, ctx).transform()
    propertyMapping.withObjectRange(Seq(range.id))
    Option(obj.default).foreach(propertyMapping.withDefault)
  }

  private def transformAnyProperty(any: AnyShape): Unit = {
    if (any.isAnyType) propertyMapping.withLiteralRange(DataType.Any)
    else {
      val range = ShapeTransformation(any, ctx).transform()
      propertyMapping.withObjectRange(Seq(range.id))
    }
    Option(any.default).foreach(propertyMapping.withDefault)
  }

  private def sanitizeScalarRange(xsd: String): String = {
    if (xsd.endsWith("#long")) {
      xsd.replace("#long", "#float")
    } else {
      xsd;
    }
  }

  private def checkMandatoriness(): Unit = {
    setWhenPresent(property.minCount, propertyMapping.withMinCount _)
  }

  private def transformEnum(shape: PropertyShape, target: PropertyMapping) = {
    val enumValues = literalValues(shape.range.values).map(AmfScalar(_))
    enumValues match {
      case scala.collection.immutable.Nil => // ignore
      case other                          => target.set(PropertyMappingModel.Enum, AmfArray(other))
    }
  }

  // @TODO: advanced types of enums
  private def literalValues(values: Seq[DataNode]): List[Any] = {
    values
      .filterType[ScalarNode]
      .collect {
        case node: ScalarNode if is(node, String)  => node.value.option()
        case node: ScalarNode if is(node, Number)  => convert(node, _.toDouble)
        case node: ScalarNode if is(node, Double)  => convert(node, _.toDouble)
        case node: ScalarNode if is(node, Float)   => convert(node, _.toFloat)
        case node: ScalarNode if is(node, Long)    => convert(node, _.toLong)
        case node: ScalarNode if is(node, Integer) => convert(node, _.toInt)
        case node: ScalarNode if is(node, Boolean) => convert(node, _.toBoolean)
      }
      .flatten
      .toList
  }

  private def convert(node: ScalarNode, conversion: String => Any) = node.value.option().map(conversion)

  private def is(node: ScalarNode, dataType: String) = node.dataType.option().contains(dataType)

  private def checkSemantics(): Unit = {
    ctx.semantics.mapping.foreach { semanticMapping =>
      semanticMapping.alias
        .option()
        .filter(alias => alias == propertyMapping.name().value())
        .foreach { _ =>
          semanticMapping.iri.option().foreach { iri =>
            propertyMapping.withNodePropertyMapping(iri)
          }
          semanticMapping.coercion.option().foreach { iri =>
            propertyMapping.withLiteralRange(iri)
          }
        }
    }
    property.range match {
      case any: AnyShape =>
        any.semanticContext.foreach { context =>
          context.typeMappings
            .flatMap(_.option())
            .headOption
            .foreach { iri =>
              propertyMapping.withNodePropertyMapping(context.expand(iri))
            }
        }

      case _ => // Ignore
    }
  }
  private def setWhenPresent[T](field: ValueField[T], setValue: T => Unit): Unit = field.option().foreach(setValue(_))
}
