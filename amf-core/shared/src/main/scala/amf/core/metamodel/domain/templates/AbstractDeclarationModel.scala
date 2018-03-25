package amf.core.metamodel.domain.templates

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, LinkableElementModel}
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType

trait AbstractDeclarationModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Schema + "name")

  val DataNode = Field(DataNodeModel, Document + "dataNode")

  val Variables = Field(Array(Str), Document + "variable")

  val Description = Field(Str, Schema + "description")

  val Usage = Field(Str, Http + "usage")

  override val key: Field = Name

  override def fields: List[Field] =
    List(Name, Description, DataNode, Variables, Usage) ++ LinkableElementModel.fields ++ DomainElementModel.fields
}

object AbstractDeclarationModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "AbstractDeclaration" :: DomainElementModel.`type`

  override def modelInstance = throw new Exception("AbstractDeclarationModel is abstract and cannot be instantiated")
}
