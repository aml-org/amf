package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.plugins.domain.webapi.models.License
import amf.core.vocabulary.Namespace.{ApiContract, Core}
import amf.core.vocabulary.ValueType

/**
  * License metamodel
  *
  * License for the API
  */
object LicenseModel extends DomainElementModel with NameFieldSchema {

  val Url = Field(Iri,
                  Core + "url",
                  ModelDoc(ModelVocabularies.Core, "url", "URL identifying the organization"))

  override val `type`: List[ValueType] = Core + "License" :: DomainElementModel.`type`

  override def fields: List[Field] = Url :: Name :: DomainElementModel.fields

  override def modelInstance = License()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "License",
    "Licensing information for a resource"
  )
}
