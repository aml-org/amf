package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDScalarElementBuilder
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  IncompatibleScalarDataType,
  UnsupportedScalarTagType,
  UnsupportedShape
}
import org.mulesoft.common.time.SimpleDateTime
import org.mulesoft.common.time.SimpleDateTime.parseDate
import org.yaml.model.{YScalar, YType}

object JsonLDScalarElementParser {
  def apply(scalar: YScalar, tagType: YType, path: JsonPath)(implicit ctx: JsonLDParserContext) = {
    val dataType = computeDatatypeFromAst(tagType).getOrElse {
      ctx.violation(UnsupportedScalarTagType, "", UnsupportedScalarTagType.message, scalar.location)
      DataTypes.String
    }
    new JsonLDScalarElementParser(scalar, tagType, path, dataType)
  }

  private def computeDatatypeFromAst(tagType: YType): Option[String] = tagType match {
    case YType.Str   => Some(DataTypes.String)
    case YType.Int   => Some(DataTypes.Integer)
    case YType.Bool  => Some(DataTypes.Boolean)
    case YType.Float => Some(DataTypes.Number)
    case YType.Null  => Some(DataTypes.Nil)
    case _           => None
  }
}

case class JsonLDScalarElementParser private (scalar: YScalar, tagType: YType, path: JsonPath, dataType: String)(
    implicit val ctx: JsonLDParserContext
) extends JsonLDBaseElementParser[JsonLDScalarElementBuilder](scalar)(ctx) {
  override def foldLeft(
      current: JsonLDScalarElementBuilder,
      other: JsonLDScalarElementBuilder
  ): JsonLDScalarElementBuilder = {
    current.merge(other)
  }

  override def unsupported(s: Shape): JsonLDScalarElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for scalar node")
    parseScalar()
  }

  override def parseNode(shape: Shape): JsonLDScalarElementBuilder = {
    shape match {
      case scalar: ScalarShape =>
        checkDataTypeConsistence(scalar)
        parseScalar(scalar.dataType.option())
      case a: AnyShape if a.isStrictAnyMeta =>
        parseScalar()
      case _ => unsupported(shape)
    }
  }

  /** Checks if the scalar data type is equivalent to the parsed tag type. DataTypes should be the same or the tag type
    * should be more specific (integer vs number shape data type)
    * @param scalarShape
    *   scalar shape defined for this node
    */
  private def checkDataTypeConsistence(scalarShape: ScalarShape): Unit = {
    scalarShape.dataType.option().foreach { shapeDataType =>
      if (dataType != shapeDataType && !isValidSpecialization(shapeDataType))
        ctx.violation(IncompatibleScalarDataType, scalarShape.id, IncompatibleScalarDataType.message, scalar.location)
    }
  }

  private def isValidSpecialization(shapeDataType: String) =
    isNumericSpecialization(shapeDataType) || isStringSpecialization(shapeDataType)

  private def isNumericSpecialization(shapeDataType: String) = {
    shapeDataType == DataTypes.Number && dataType == DataTypes.Integer
  }

  private def isStringSpecialization(shapeDataType: String) = {
    (shapeDataType == DataTypes.Date || shapeDataType == DataTypes.DateTime) && dataType == DataTypes.String
  }

  /** @return
    *   a jsondl scalar builder for the given YScalar value and dataType computed from the YType.
    */

  private def parseScalar(expectedDataType: Option[String] = None): JsonLDScalarElementBuilder = {
    val (finalDataType, value) = (expectedDataType, dataType, scalar.value) match {
      case (_, DataTypes.Nil, _)               => (dataType, "null")
      case (_, DataTypes.Integer, value: Long) => (dataType, value.toInt)
      case (Some(DataTypes.Date), DataTypes.String, value: String) =>
        parseDate(value).map(value => (DataTypes.Date, value)).getOrElse((DataTypes.String, value))
      case (Some(DataTypes.DateTime), DataTypes.String, value: String) =>
        SimpleDateTime.parse(value).map(value => (DataTypes.DateTime, value)).getOrElse((DataTypes.String, value))
      case (_, _, value) => (dataType, value)
    }
    val annotation = Annotations(scalar)
    new JsonLDScalarElementBuilder(finalDataType, value, annotation, path = path)
  }
}
