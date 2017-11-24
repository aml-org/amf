package amf.framework.metamodel.domain.templates

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.domain.DomainElementModel
import amf.framework.model.domain.templates.VariableValue
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

object VariableValueModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Value = Field(Str, Document + "value")

  override def fields: List[Field] = List(Name, Value) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "VariableValue" :: DomainElementModel.`type`

  override def modelInstance = VariableValue()
}
