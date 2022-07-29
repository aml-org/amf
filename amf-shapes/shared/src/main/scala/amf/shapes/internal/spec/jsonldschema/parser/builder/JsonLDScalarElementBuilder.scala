package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.platform.model.DataTypes
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleScalarDataType
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.mulesoft.common.client.lexical.SourceLocation

class JsonLDScalarElementBuilder(var dataType: String, var value: Any, override val location: SourceLocation)
    extends JsonLDElementBuilder(location) {
  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDScalarElementBuilder]

  override def merge(
      other: JsonLDScalarElementBuilder.this.type
  )(implicit ctx: ShapeTransformationContext): JsonLDScalarElementBuilder.this.type = {
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
}

object JsonLDScalarElementBuilder {
  def empty() = new JsonLDScalarElementBuilder(DataTypes.Nil, null, SourceLocation.Unknown)
}
