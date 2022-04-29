package amf.apicontract.internal.metamodel.domain.security

import amf.apicontract.client.scala.model.domain.security.Scope
import amf.core.client.scala.vocabulary.Namespace.{Core, Security}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object ScopeModel extends DomainElementModel {

  val Name = Field(Str, Core + "name", ModelDoc(ModelVocabularies.Core, "name", "Name of the scope"))

  val Description = Field(
    Str,
    Core + "description",
    ModelDoc(ModelVocabularies.Security, "description", "Human readable description for the scope")
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
