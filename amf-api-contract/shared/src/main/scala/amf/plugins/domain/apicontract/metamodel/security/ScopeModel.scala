package amf.plugins.domain.apicontract.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.plugins.domain.apicontract.models.security.Scope
import amf.core.vocabulary.Namespace.{Security, Core}
import amf.core.vocabulary.{Namespace, ValueType}

object ScopeModel extends DomainElementModel {

  val Name = Field(Str, Core + "name", ModelDoc(ModelVocabularies.Core, "name", "Name of the scope"))

  val Description = Field(
    Str,
    Core + "description",
    ModelDoc(ModelVocabularies.Security, "description", "Human readable description for the scope"))

  override def fields: List[Field] = List(Name, Description) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "Scope") ++ DomainElementModel.`type`

  override def modelInstance = Scope()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Scope",
    ""
  )
}
