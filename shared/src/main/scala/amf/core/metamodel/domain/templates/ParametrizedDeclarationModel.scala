package amf.core.metamodel.domain.templates

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

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