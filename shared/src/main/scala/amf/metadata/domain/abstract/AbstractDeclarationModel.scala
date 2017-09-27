package amf.metadata.domain.`abstract`

import amf.metadata.Field
import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.DomainElementModel
import amf.metadata.domain.extensions.DataNodeModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

trait AbstractDeclarationModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val DataNode = Field(DataNodeModel, Document + "dataNode")

  val Variables = Field(Array(VariableModel), Document + "variable")
}

object AbstractDeclarationModel extends AbstractDeclarationModel {
  override val fields: List[Field] = List(Name, DataNode, Variables) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "AbstractDeclaration" :: DomainElementModel.`type`
}

object ResourceTypeModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "ResourceType" :: AbstractDeclarationModel.`type`

  override def fields: List[Field] = AbstractDeclarationModel.fields
}

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "Trait" :: AbstractDeclarationModel.`type`

  override def fields: List[Field] = AbstractDeclarationModel.fields
}
