package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{PropertyLikeMapping, PropertyMapping}
import amf.aml.internal.metamodel.domain.PropertyMappingModel
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.DataType._
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape}
import org.mulesoft.common.collections.FilterType

case class PropertyShapeTransformer(property: PropertyShape, ctx: ShapeTransformationContext) {

  val propertyMapping: PropertyMapping = PropertyMapping(Fields(), Annotations(property.annotations))

  def transform(): PropertyMapping = {

    setMappingName()
    setMappingID()

    property.range match {
      case scalar: ScalarShape => transformScalarProperty(scalar)
      case obj: NodeShape      => transformObjectProperty(obj)
      case array: ArrayShape =>
        propertyMapping.withAllowMultiple(true)
        array.items match {
          case scalar: ScalarShape => transformScalarProperty(scalar)
          case obj: NodeShape      => transformObjectProperty(obj)
        }
      case any: AnyShape => transformAnyProperty(any)
    }
    checkMandatoriness()
    checkSemantics()
    transformEnum(property, propertyMapping)
    propertyMapping
  }

  private def setMappingName(): Unit = propertyMapping.withName(property.name.value().replaceAll(" ", ""))

  private def setMappingID(): Unit = propertyMapping.withId(property.id)

  private def transformScalarProperty(scalar: ScalarShape): Unit = {
    // datatype
    val scalarRangeDatatype = sanitizeScalarRange(scalar.dataType.value())
    propertyMapping.withLiteralRange(scalarRangeDatatype)
    // pattern
    scalar.pattern.option().foreach { pattern =>
      propertyMapping.withPattern(pattern)
    }
    scalar.minimum.option().foreach { minimum =>
      propertyMapping.withMinimum(minimum)
    }
    scalar.maximum.option().foreach { maximum =>
      propertyMapping.withMaximum(maximum)
    }
    /*
    scalar.values.filter {
      case _: ScalarNode => true
      case _             => false // @TODO: advanced types of enums
    } map { s =>

    }
   */
  }

  def transformObjectProperty(obj: NodeShape): Unit = {
    val range = ShapeTransformation(obj, ctx).transform()
    propertyMapping.withObjectRange(Seq(range.id))
  }

  def transformAnyProperty(any: AnyShape): Unit = {
    if (any.isAnyType) propertyMapping.withLiteralRange(DataType.Any)
    else {
      val range = ShapeTransformation(any, ctx).transform()
      propertyMapping.withObjectRange(Seq(range.id))
    }
  }

  private def sanitizeScalarRange(xsd: String): String = {
    if (xsd.endsWith("#long")) {
      xsd.replace("#long", "#float")
    } else {
      xsd;
    }
  }

  private def checkMandatoriness(): Unit = {
    property.minCount.option().foreach { minCount =>
      propertyMapping.withMinCount(minCount)
    }
  }

  private def transformEnum(shape: PropertyShape, target: PropertyMapping) = {
    val enumValues = literalValues(shape.range.values).map(AmfScalar(_))
    enumValues match {
      case scala.collection.immutable.Nil => // ignore
      case other                          => target.set(PropertyMappingModel.Enum, AmfArray(other))
    }
  }

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

  def checkSemantics(): Unit = {
    ctx.semantics.mapping.foreach { semanticMapping =>
      val alias = semanticMapping.alias.value()
      if (propertyMapping.name().value() == alias) {
        semanticMapping.iri.option().foreach { iri =>
          propertyMapping.withNodePropertyMapping(iri)
        }
        semanticMapping.coercion.option().foreach { iri =>
          propertyMapping.withLiteralRange(iri)
        }
      }
    }
    property.range match {
      case any: AnyShape if any.semanticContext.nonEmpty =>
        val semanticContext = any.semanticContext.get
        semanticContext.typeMappings.map(_.value()).headOption match {
          case Some(iri) =>
            propertyMapping.withNodePropertyMapping(semanticContext.expand(iri))
          case None => // Ignore
        }
      case _ => // Ignore
    }
  }

}
