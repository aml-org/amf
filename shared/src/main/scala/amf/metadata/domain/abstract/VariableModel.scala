package amf.metadata.domain.`abstract`

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object VariableModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Transformation = Field(Str, Document + "transformation")

  override val fields: List[Field] = List(Name, Transformation) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "Variable" :: DomainElementModel.`type`
}
