package amf.core.metamodel.domain.templates

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.templates.VariableValue
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object VariableValueModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Value = Field(Str, Document + "value")

  override def fields: List[Field] = List(Name, Value) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "VariableValue" :: DomainElementModel.`type`

  override def modelInstance = VariableValue()
}
