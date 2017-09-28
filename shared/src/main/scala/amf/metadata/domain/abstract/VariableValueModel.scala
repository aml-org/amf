package amf.metadata.domain.`abstract`

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object VariableValueModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Value = Field(Str, Document + "value")

  override val fields: List[Field] = List(Name, Value) ++ Option(DomainElementModel.fields).getOrElse(Seq())

  override val `type`: List[ValueType] = Document + "VariableValue" :: Option(DomainElementModel.`type`).getOrElse(List())
}
