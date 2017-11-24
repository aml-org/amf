package amf.framework.metamodel.domain.templates

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Iri, Str}
import amf.framework.metamodel.domain.DomainElementModel
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

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