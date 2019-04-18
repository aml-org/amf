package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.plugins.domain.webapi.models.security.Scope
import amf.core.vocabulary.Namespace.Security
import amf.core.vocabulary.{Namespace, ValueType}

object ScopeModel extends DomainElementModel {

  val Name = Field(
    Str,
    Security + "name",
    ModelDoc(ModelVocabularies.Security, "name", "Name of the scope", Seq((Namespace.Core + "name").iri())))

  val Description = Field(
    Str,
    Security + "description",
    ModelDoc(ModelVocabularies.Security,
             "description",
             "Human readable description for the scope",
             Seq((Namespace.Core + "description").iri()))
  )

  override def fields: List[Field] = List(Name, Description) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Security + "Scope") ++ DomainElementModel.`type`

  override def modelInstance = Scope()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Scope",
    ""
  )
}
