package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.Shape
import amf.core.parser._
import amf.plugins.document.webapi.parser.ShapeParserContext
import amf.plugins.document.webapi.parser.spec.common.QuickFieldParserOps
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef.{DoubleType, FloatType, IntType, LongType}
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import amf.validations.ShapeParserSideValidations.InvalidShapeFormat
import org.yaml.model.{YMap, YScalar}

object FormatValidator {
  def isValid(format: String, typeDef: TypeDef): Boolean = {
    if (typeDef.isNumber)
      typeDef match {
        case TypeDef.IntType if !List("int", "int8", "int16", "int32", "int64", "long").contains(format) => false
        case FloatType if format.equals("double")                                                        => false // should no be possible
        case LongType if List("double", "float").contains(format)                                        => false // should not be possible either
        case _ if !VALID_NUMBER_FORMATS.contains(format)                                                 => false
        case _                                                                                           => true
      } else !VALID_NUMBER_FORMATS.contains(format)
  }

  val VALID_NUMBER_FORMATS = List("int", "int8", "int16", "int32", "int64", "long", "float", "double")
}

case class ScalarFormatType(shape: Shape, typeDef: TypeDef)(implicit ctx: ShapeParserContext) extends QuickFieldParserOps {
  def parse(map: YMap): TypeDef = {
    map
      .key("format")
      .map { n =>
        val format = n.value.as[YScalar].text

        if (!FormatValidator.isValid(format, typeDef))
          ctx.eh.warning(InvalidShapeFormat,
                         shape.id,
                         s"Format $format is not valid for type ${XsdTypeDefMapping.xsd(typeDef)}",
                         n)

        (ScalarShapeModel.Format in shape).allowingAnnotations(n)
        fromFormat(format)
      }
      .getOrElse(typeDef)
  }

  private def fromFormat(format: String) = {
    if (typeDef.isNumber) formatType(format).getOrElse(typeDef)
    else typeDef
  }

  private def formatType(format: String): Option[TypeDef] = {
    format match {
      case "int" | "int8" | "int16" | "int32" => Some(IntType)
      case "int64" | "long"                   => Some(LongType)
      case "double"                           => Some(DoubleType)
      case "float"                            => Some(FloatType)
      case _                                  => None
    }
  }

}
