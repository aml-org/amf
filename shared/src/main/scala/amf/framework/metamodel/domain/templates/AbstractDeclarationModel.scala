package amf.framework.metamodel.domain.templates

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Str}
import amf.framework.metamodel.domain.{DataNodeModel, DomainElementModel}
import amf.vocabulary.Namespace._
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

  override def modelInstance = throw new Exception("AbstractDeclarationModel is abstract and cannot be instantiated")
}