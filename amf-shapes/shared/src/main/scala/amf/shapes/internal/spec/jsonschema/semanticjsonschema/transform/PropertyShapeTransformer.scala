package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.PropertyMapping
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape}

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
  }

}
