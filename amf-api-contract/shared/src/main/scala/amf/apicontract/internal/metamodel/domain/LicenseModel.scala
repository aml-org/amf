package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.License
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Iri, Str}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

/** License metamodel
  *
  * License for the API
  */
object LicenseModel extends DomainElementModel with NameFieldSchema {

  val Url = Field(Iri, Core + "url", ModelDoc(ModelVocabularies.Core, "url", "URL identifying the organization"))

  val Identifier = Field(
    Str,
    Core + "identifier",
    ModelDoc(ModelVocabularies.Core, "identifier", "SPDX license expression for the API")
  )

  override val `type`: List[ValueType] = Core + "License" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Name :: Identifier :: DomainElementModel.fields

  override def modelInstance = License()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "License",
    "Licensing information for a resource"
  )
}
