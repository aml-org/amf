package amf.plugins.parser.dialect

import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.shapes.models.{ArrayShape, ScalarShape}

case class PropertyShapeTransformer(property: PropertyShape, ctx: ShapeTransformationContext) {

  val propertyMapping = PropertyMapping(Fields(),Annotations(property.annotations))
    .withId(property.id)
    .withName(property.name.value())

  def transform(): PropertyMapping = {
    property.range match {
      case scalar: ScalarShape =>
        transformScalarProperty(scalar)
      case array: ArrayShape => {
        propertyMapping.withAllowMultiple(true)
        array.items match {
          case scalar: ScalarShape => transformScalarProperty(scalar)
        }
      }
    }
    propertyMapping
  }

  private def transformScalarProperty(scalar: ScalarShape) = {
    val scalarRangeDatatype = sanitizeScalarRange(scalar.dataType.value())
    propertyMapping.withLiteralRange(scalarRangeDatatype)
    scalar.pattern.option().foreach { pattern =>
      propertyMapping.withPattern(pattern)
    }
  }

  private def sanitizeScalarRange(xsd: String): String = {
    if (xsd.endsWith("#long")) {
      xsd.replace("#long", "#float");
    } else {
      xsd;
    }
  }
/*
  def checkSemantics(): Unit = {
    ctx.semantics.mapping.foreach {

    }
  }
*/
}
