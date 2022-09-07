package amf.apicontract.internal.validation.shacl.graphql.values

import amf.apicontract.internal.validation.shacl.graphql.GraphQLDataTypes.friendlyName
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object ScalarValueValidator extends ValueValidator[ScalarShape] {
  override def validate(shape: ScalarShape, value: DataNode): Seq[ValidationInfo] = {
    value match {
      case s: ScalarNode => validateDataType(shape, s)
      case a: ArrayNode  => Seq(typeError("scalar", "list", a.annotations))
      case o: ObjectNode => Seq(typeError("scalar", "object", o.annotations))
    }
  }

  private def validateDataType(shape: ScalarShape, value: ScalarNode): Seq[ValidationInfo] = {
    val shapeDT = shape.dataType.value()
    val valueDT = value.dataType.value()
    shapeDT match {
      case DataTypes.Any if !isNull(value) => Nil // custom scalars are 'Any'. These accept all values except 'null'
      case shapeDT if shapeDT != valueDT =>
        val expected = friendlyName(shape)
        val actual   = friendlyName(valueDT)
        Seq(typeError(expected, actual, value.annotations))
      case _ => Nil
    }
  }
}
