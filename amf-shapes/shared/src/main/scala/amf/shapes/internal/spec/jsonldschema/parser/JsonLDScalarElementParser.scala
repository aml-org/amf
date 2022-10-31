package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape, SemanticContext}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDScalarElementBuilder
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  IncompatibleScalarDataType,
  UnsupportedScalarTagType,
  UnsupportedShape
}
import org.yaml.model.{YScalar, YType}

case class JsonLDScalarElementParser(scalar: YScalar, tagType: YType, path: JsonPath)(implicit
    val ctx: JsonLDParserContext
) extends JsonLDBaseElementParser[JsonLDScalarElementBuilder](scalar)(ctx) {
  override def foldLeft(
      current: JsonLDScalarElementBuilder,
      other: JsonLDScalarElementBuilder
  ): JsonLDScalarElementBuilder = {
    current.merge(other)
  }

  override def unsupported(s: Shape): JsonLDScalarElementBuilder = {
    ctx.violation(UnsupportedShape, s.id, "Invalid shape class for scalar node")
    parseScalar(None)
  }

  override def parseNode(shape: Shape): JsonLDScalarElementBuilder = {
    shape match {
      case scalar: ScalarShape =>
        checkDataTypeConsistence(scalar)
        parseScalar(scalar.semanticContext)
      case a: AnyShape if a.meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
        parseScalar(a.semanticContext)
      case _ => unsupported(shape)
    }
  }

  val dataType: String = tagType match {
    case YType.Str   => DataTypes.String
    case YType.Int   => DataTypes.Integer
    case YType.Bool  => DataTypes.Boolean
    case YType.Float => DataTypes.Number
    case YType.Null  => DataTypes.Nil
    case _ =>
      ctx.violation(UnsupportedScalarTagType, "", UnsupportedScalarTagType.message, scalar.location)
      DataTypes.String
  }

  /** Checks if the scalar data type is equivalent to the parsed tag type. DataTypes should be the same or the tag type
    * should be more specific (integer vs number shape data type)
    * @param scalarShape
    *   scalar shape defined for this node
    */
  def checkDataTypeConsistence(scalarShape: ScalarShape): Unit = {
    scalarShape.dataType.option().foreach { shapeDataType =>
      if (dataType != shapeDataType && !(shapeDataType == DataTypes.Number && dataType == DataTypes.Integer))
        ctx.violation(IncompatibleScalarDataType, scalarShape.id, IncompatibleScalarDataType.message, scalar.location)
    }
  }

  /** @return
    *   a jsondl scalar builder for the given YScalar value and dataType computed from the YType.
    */
  def parseScalar(semanticContext: Option[SemanticContext]): JsonLDScalarElementBuilder = {
    val builder =
      if (dataType == DataTypes.Nil)
        new JsonLDScalarElementBuilder(dataType, "null", location = scalar.location, path = path)
      else new JsonLDScalarElementBuilder(dataType, scalar.value, location = scalar.location, path = path)

    builder
  }

}
