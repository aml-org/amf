package amf.metadata.domain.`abstract`

import amf.domain.`abstract`.VariableValue
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object VariableValueModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Value = Field(Str, Document + "value")

  override def fields: List[Field] = List(Name, Value) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "VariableValue" :: DomainElementModel.`type`

  override def modelInstance = VariableValue()
}
