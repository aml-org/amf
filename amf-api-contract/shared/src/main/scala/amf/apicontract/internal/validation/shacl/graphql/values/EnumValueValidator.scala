package amf.apicontract.internal.validation.shacl.graphql.values

import amf.apicontract.internal.validation.shacl.graphql.GraphQLDataTypes.friendlyName
import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object EnumValueValidator extends ValueValidator[ScalarShape] {
  override def validate(shape: ScalarShape, value: DataNode): Seq[ValidationInfo] = {
    value match {
      case s: ScalarNode =>
        validateDataType(s) ++ validateValueIsMember(shape, s)
      case a: ArrayNode  => Seq(typeError("scalar", "list", a.annotations))
      case o: ObjectNode => Seq(typeError("scalar", "object", o.annotations))
    }
  }
  private def validateDataType(value: ScalarNode): Seq[ValidationInfo] = {
    value.dataType.value() match {
      case DataTypes.Any => Nil // enum values are 'Any' explicitly
      case otherDT       => Seq(typeError("enum", friendlyName(otherDT), value.annotations))
    }
  }

  private def validateValueIsMember(shape: ScalarShape, value: ScalarNode): Seq[ValidationInfo] = {
    val acceptedValues = shape.values
    val actualValue    = value.value.value()

    val isAccepted = acceptedValues.exists {
      case acceptedScalar: ScalarNode => acceptedScalar.value.value() == actualValue
      case _                          => false
    }

    if (isAccepted) {
      Nil
    } else {
      val enumName = shape.name.value()
      val message  = s"Value '$actualValue' is not a member of enum '$enumName'"
      Seq(ValidationInfo(ScalarNodeModel.Value, Some(message), Some(value.annotations)))
    }
  }
}
