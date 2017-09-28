package amf.metadata.domain.`abstract`

import amf.metadata.Field
import amf.metadata.Type.{Array, Iri, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

trait ParametrizedDeclarationModel extends DomainElementModel {

  val Name = Field(Str, Document + "name")

  val Target = Field(Iri, Document + "target")

  val Variables = Field(Array(VariableValueModel), Document + "variable")
}

object ParametrizedDeclarationModel extends ParametrizedDeclarationModel {
  override val fields: List[Field] = List(Name, Target, Variables) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "ParametrizedDeclaration" :: DomainElementModel.`type`
}

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedTrait" :: ParametrizedDeclarationModel.`type`

  override def fields: List[Field] = ParametrizedDeclarationModel.fields
}

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedResourceType" :: ParametrizedDeclarationModel.`type`

  override def fields: List[Field] = ParametrizedDeclarationModel.fields
}
