package amf.apicontract.internal.validation.shacl.graphql.values

import amf.apicontract.internal.validation.shacl.graphql.GraphQLUtils
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.ArrayShape
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object ListValueValidator extends ValueValidator[ArrayShape] {
  override def validate(shape: ArrayShape, value: DataNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    if (!GraphQLUtils.isInsideRootType(shape)) {
      value match {
        case a: ArrayNode               => validateItems(shape, a)
        case s: ScalarNode if isNull(s) => Seq(typeError("list", "null", s.annotations))
        case s: ScalarNode              => Seq(typeError("list", "scalar", s.annotations))
        case o: ObjectNode              => Seq(typeError("list", "object", o.annotations))
      }
    } else Nil
  }

  private def validateItems(shape: ArrayShape, values: ArrayNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    val items = shape.items
    values.members.flatMap { value =>
      ValueValidator.validate(items, value)
    }
  }
}
