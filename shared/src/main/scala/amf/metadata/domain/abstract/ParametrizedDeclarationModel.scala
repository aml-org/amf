package amf.metadata.domain.`abstract`

import amf.domain.`abstract`.{ParametrizedResourceType, ParametrizedTrait}
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Iri, Str}
import amf.metadata.domain.{DomainElementModel, KeyField}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

trait ParametrizedDeclarationModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Document + "name")

  val Target = Field(Iri, Document + "target")

  val Variables = Field(Array(VariableValueModel), Document + "variable")

  override val key: Field = Name

  override def fields: List[Field] = List(Name, Target, Variables) ++ DomainElementModel.fields
}

object ParametrizedDeclarationModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedDeclaration" :: DomainElementModel.`type`

  override def modelInstance = throw new Exception("ParametrizedDeclaration is abstract and cannot be instantiated by default")
}

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedTrait" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedTrait()
}

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedResourceType" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedResourceType()
}
