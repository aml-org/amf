package amf.metadata.domain.`abstract`

import amf.domain.`abstract`.{ResourceType, Trait}
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Str}
import amf.metadata.domain.{DomainElementModel, KeyField}
import amf.metadata.domain.extensions.DataNodeModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

trait AbstractDeclarationModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Document + "name")

  val DataNode = Field(DataNodeModel, Document + "dataNode")

  val Variables = Field(Array(Str), Document + "variable")

  override val key: Field = Name

  override def fields: List[Field] = List(Name, DataNode, Variables) ++ DomainElementModel.fields
}

object AbstractDeclarationModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "AbstractDeclaration" :: DomainElementModel.`type`

  override def modelInstance = throw new Exception("Abstract declaration is abstract, cannot create default model instance")
}

object ResourceTypeModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()
}

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "Trait" :: AbstractDeclarationModel.`type`

  override def modelInstance = Trait()
}
