package amf.plugins.parser.dialect

import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, Fields, ScalarNode}
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.shapes.models.{ArrayShape, ScalarShape}

case class PropertyShapeTransformer(property: PropertyShape, ctx: ShapeTransformationContext) {

  val propertyMapping = PropertyMapping(Fields(),Annotations(property.annotations))
    .withId(property.id)
    .withName(property.name.value())

  def checkMandatoriness() = {
    property.minCount.option().foreach { minCount =>
      propertyMapping.withMinCount(minCount)
    }
  }

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
    checkMandatoriness()
    checkSemantics()
    propertyMapping
  }

  private def transformScalarProperty(scalar: ScalarShape) = {
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

  private def sanitizeScalarRange(xsd: String): String = {
    if (xsd.endsWith("#long")) {
      xsd.replace("#long", "#float");
    } else {
      xsd;
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
