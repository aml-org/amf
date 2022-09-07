package amf.apicontract.internal.validation.shacl.graphql.values

import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.shapes.client.scala.model.domain.ArrayShape
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object ListValueValidator extends ValueValidator[ArrayShape] {
  override def validate(shape: ArrayShape, value: DataNode): Seq[ValidationInfo] = {
    value match {
      case a: ArrayNode               => validateItems(shape, a)
      case s: ScalarNode if isNull(s) => Seq(typeError("list", "null", s.annotations))
      case s: ScalarNode              => Seq(typeError("list", "scalar", s.annotations))
      case o: ObjectNode              => Seq(typeError("list", "object", o.annotations))
    }
  }

  private def validateItems(shape: ArrayShape, values: ArrayNode): Seq[ValidationInfo] = {
    val items = shape.items
    values.members.flatMap { value =>
      ValueValidator.validate(items, value)
    }
  }
}
