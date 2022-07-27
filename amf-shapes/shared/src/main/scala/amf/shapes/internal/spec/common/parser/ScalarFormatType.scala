package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.common.TypeDef.{DoubleType, FloatType, IntType, LongType}
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidShapeFormat
import org.yaml.model.{YMap, YScalar}

object FormatValidator {
  def isValid(format: String, typeDef: TypeDef): Boolean = {
    if (typeDef.isNumber)
      typeDef match {
        case TypeDef.IntType if !List("int", "int8", "int16", "int32", "int64", "long").contains(format) => false
        case FloatType if format.equals("double")                 => false // should no be possible
        case LongType if List("double", "float").contains(format) => false // should not be possible either
        case _ if !VALID_NUMBER_FORMATS.contains(format)          => false
        case _                                                    => true
      }
    else !VALID_NUMBER_FORMATS.contains(format)
  }

  val VALID_NUMBER_FORMATS = List("int", "int8", "int16", "int32", "int64", "long", "float", "double")
}

case class ScalarFormatType(shape: Shape, typeDef: TypeDef)(implicit ctx: ShapeParserContext)
    extends QuickFieldParserOps {
  def parse(map: YMap): TypeDef = {
    map
      .key("format")
      .map { n =>
        val format = n.value.as[YScalar].text

        if (!FormatValidator.isValid(format, typeDef))
          ctx.eh.warning(
            InvalidShapeFormat,
            shape,
            s"Format $format is not valid for type ${XsdTypeDefMapping.xsd(typeDef)}",
            n.location
          )

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
