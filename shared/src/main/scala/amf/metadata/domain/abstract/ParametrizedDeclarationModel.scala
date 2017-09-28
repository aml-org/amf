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
  override val fields: List[Field] = List(Name, Target, Variables) ++ Option(DomainElementModel.fields).getOrElse(List())

  override val `type`: List[ValueType] = Document + "ParametrizedDeclaration" :: Option(DomainElementModel.`type`).getOrElse(List())
}

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedTrait" :: Option(ParametrizedDeclarationModel.`type`).getOrElse(List())

  override def fields: List[Field] = Option(ParametrizedDeclarationModel.fields).getOrElse(List())
}

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedResourceType" :: Option(ParametrizedDeclarationModel.`type`).getOrElse(List())

  override def fields: List[Field] = Option(ParametrizedDeclarationModel.fields).getOrElse(List())
}