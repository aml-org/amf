package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.internal.metamodel.Type
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDScalar}
import amf.shapes.internal.spec.jsonldschema.parser.{JsonLDParserContext, JsonPath}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleScalarDataType
import org.mulesoft.common.client.lexical.{PositionRange, SourceLocation}
import org.yaml.model.YValue

class JsonLDScalarElementBuilder(
    var dataType: String,
    var value: Any,
    override val annotation: Annotations,
    path: JsonPath
) extends JsonLDElementBuilder(annotation, path) {

  override type THIS = JsonLDScalarElementBuilder

  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDScalarElementBuilder]

  override def merge(
      other: JsonLDScalarElementBuilder
  )(implicit ctx: JsonLDParserContext): JsonLDScalarElementBuilder = {
    super.merge(other)

    dataType match {
      case _ if other.dataType == dataType =>
        dataType =
          other.dataType // it's the same. Value could be different? using the specific one, value for a property of a node should be always the same
      case DataTypes.Number if other.dataType == DataTypes.Integer =>
        dataType = other.dataType
      case _ =>
        ctx.violation(IncompatibleScalarDataType, "", IncompatibleScalarDataType.message)
        dataType = other.dataType
        value = other.value
    }
    this
  }

  override def build(ctxBuilder: EntityContextBuilder): (JsonLDElement, Type) = {
    (new JsonLDScalar(value, dataType), getType)
  }

  private def getType = {
    dataType match {
      case DataTypes.Number   => Type.Float
      case DataTypes.Integer  => Type.Int
      case DataTypes.Boolean  => Type.Bool
      case DataTypes.Nil      => Type.Null
      case DataTypes.Date     => Type.Date
      case DataTypes.DateTime => Type.DateTime
      case _                  => Type.Str
    }
  }
}

object JsonLDScalarElementBuilder {
  def empty(path: JsonPath) = new JsonLDScalarElementBuilder(DataTypes.Nil, null, Annotations.virtual(), path)
}
