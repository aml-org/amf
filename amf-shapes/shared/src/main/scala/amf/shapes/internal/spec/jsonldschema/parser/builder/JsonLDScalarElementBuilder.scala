package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDScalar}
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleScalarDataType
import org.mulesoft.common.client.lexical.SourceLocation

class JsonLDScalarElementBuilder(var dataType: String, var value: Any, override val location: SourceLocation)
    extends JsonLDElementBuilder(location) {

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
        ctx.violation(IncompatibleScalarDataType, "", IncompatibleScalarDataType.message, location)
        dataType = other.dataType
        value = other.value
    }
    this
  }

  override def build(ctxBuilder: EntityContextBuilder): JsonLDElement = {
    val scalar = new JsonLDScalar(classTerms.toList).withValue(value).withDataType(dataType)
    ctxBuilder + scalar.meta
    scalar
  }
}

object JsonLDScalarElementBuilder {
  def empty() = new JsonLDScalarElementBuilder(DataTypes.Nil, null, SourceLocation.Unknown)
}
