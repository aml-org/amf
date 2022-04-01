package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.PropertyMapping
import amf.aml.internal.metamodel.domain.PropertyMappingModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, ScalarNode}
import amf.core.client.scala.model.{DataType, ValueField}
import amf.shapes.client.scala.model.domain._
import org.mulesoft.common.collections.FilterType

class PropertyShapeTransformer(property: PropertyShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler) {

  val mapping: PropertyMapping = PropertyMapping(property.annotations)

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
    transformEnum(property, mapping)
    mapping
  }

  private def transformArray(array: ArrayShape): Unit = {
    mapping.withAllowMultiple(true)
    array.items match {
      case scalar: ScalarShape => transformScalarProperty(scalar)
      case obj: NodeShape      => transformObjectProperty(obj)
      case any: AnyShape       => transformAnyProperty(any)
    }
    Option(array.default).foreach(mapping.withDefault)
  }

  private def setMappingName(): Unit = mapping.withName(property.name.value().replaceAll(" ", ""))

  private def setMappingID(): Unit = mapping.withId(property.id)

  private def transformScalarProperty(scalar: ScalarShape): Unit = {
    // datatype
    scalar.dataType.option().foreach { dataType =>
      mapping.withLiteralRange(sanitizeScalarRange(dataType))
    }
    // pattern
    setWhenPresent(scalar.pattern, mapping.withPattern)
    setWhenPresent(scalar.minimum, mapping.withMinimum)
    setWhenPresent(scalar.maximum, mapping.withMaximum)
    Option(scalar.default).foreach(mapping.withDefault)
  }

  private def transformObjectProperty(obj: NodeShape): Unit = {
    val range = ShapeTransformation(obj, ctx).transform()
    mapping.withObjectRange(Seq(range.id))
    Option(obj.default).foreach(mapping.withDefault)
  }

  private def transformAnyProperty(any: AnyShape): Unit = {
    if (any.isAnyType) mapping.withLiteralRange(DataType.Any)
    else {
      val range = ShapeTransformation(any, ctx).transform()
      mapping.withObjectRange(Seq(range.id))
    }
    Option(any.default).foreach(mapping.withDefault)
  }

  private def sanitizeScalarRange(range: String): String = {
    if (range == DataType.Long) DataType.Float
    else if (range == DataType.Number) DataType.Double
    else range
  }

  private def checkMandatoriness(): Unit = {
    setWhenPresent(property.minCount, mapping.withMinCount)
  }

  private def transformEnum(shape: PropertyShape, target: PropertyMapping) = {
    val enumValues = scalarValues(shape.range.values)
    enumValues match {
      case scala.collection.immutable.Nil => // ignore
      case other                          => target.set(PropertyMappingModel.Enum, AmfArray(other))
    }
  }

  // @TODO: advanced types of enums
  private def scalarValues(values: Seq[DataNode]): List[AmfScalar] = {
    values
      .filterType[ScalarNode]
      .collect {
        case node: ScalarNode if is(node, String)  => convert(node, value => value)
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

  private def convert(node: ScalarNode, conversion: String => Any): Option[AmfScalar] =
    node.value
      .option()
      .map(conversion)
      .map(value => AmfScalar(value, node.annotations))

  private def is(node: ScalarNode, dataType: String) = node.dataType.option().contains(dataType)

  private def checkSemantics(): Unit = {
    ctx.semantics.mapping.foreach { semanticMapping =>
      semanticMapping.alias
        .option()
        .filter(alias => alias == mapping.name().value())
        .foreach { _ =>
          semanticMapping.iri.option().foreach { iri =>
            mapping.withNodePropertyMapping(iri)
          }
          semanticMapping.coercion.option().foreach { iri =>
            mapping.withLiteralRange(iri)
          }
        }
    }
    property.range match {
      case any: AnyShape =>
        any.semanticContext.foreach { localContext =>
          val context = ctx.semantics.merge(localContext).normalize()
          localContext.typeMappings
            .flatMap(_.option())
            .toList match {
            // If there is only one semantic, I will set it
            case List(element) => mapping.withNodePropertyMapping(context.expand(element))
            case List(Nil)     => // ignore
            // If there is more than one, I will collect it to generate a vocab a the end of the process
            case elements =>
              ctx.termsToExtract += CandidateProperty(mapping, elements)
          }
        }

      case _ => // Ignore
    }
  }

  private def setWhenPresent[T](field: ValueField[T], setValue: T => Unit): Unit = field.option().foreach(setValue(_))
}

object PropertyShapeTransformer {
  def apply(property: PropertyShape, ctx: ShapeTransformationContext)(implicit errorHandler: AMFErrorHandler) =
    new PropertyShapeTransformer(property, ctx)
}
