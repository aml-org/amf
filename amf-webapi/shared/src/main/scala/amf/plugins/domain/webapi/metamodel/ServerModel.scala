package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.vocabulary.Namespace.{Core, ApiContract}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Server

/**
  * Server meta model
  */
object ServerModel extends DomainElementModel with DescriptionField {
  val Url = Field(Str, ApiContract + "url", ModelDoc(ModelVocabularies.ApiContract, "url", "URL (potentially a template) for the server"))

  val Variables = Field(Array(ParameterModel),
                        ApiContract + "variable",
                        ModelDoc(ModelVocabularies.ApiContract, "variable", "Variables in the URL for the server"))

  override val `type`: List[ValueType] = ApiContract + "Server" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Url,
      Description,
      Variables
    ) ++ DomainElementModel.fields

  override def modelInstance = Server()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Server",
    "Information about the network accessible locations where the API is available"
  )
}
