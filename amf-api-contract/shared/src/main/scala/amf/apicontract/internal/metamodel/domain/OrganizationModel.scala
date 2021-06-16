package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Organization
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Iri, Str}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

/**
  * Organization metamodel
  */
object OrganizationModel extends DomainElementModel with NameFieldSchema {

  val Url = Field(Iri, Core + "url", ModelDoc(ModelVocabularies.Core, "url", "URL identifying the organization"))

  val Email =
    Field(Str, Core + "email", ModelDoc(ModelVocabularies.Core, "email", "Contact email for the organization"))

  override val `type`: List[ValueType] = Core + "Organization" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Url, Name, Email) ++ DomainElementModel.fields

  override def modelInstance = Organization()

  override val doc: ModelDoc =
    ModelDoc(ModelVocabularies.Core, "Organization", "Organization providing an good or service")
}
