package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.webapi.metamodel.{ParameterModel, ResponseModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema, Security}
import amf.core.vocabulary.{Namespace, ValueType}

object SecuritySchemeModel extends DomainElementModel with KeyField with DescriptionField with DisplayNameField {

  val Name = Field(Str,
                   Security + "name",
                   ModelDoc(ModelVocabularies.Security,
                            "name",
                            "Name for the security scheme",
                            Seq((Namespace.Schema + "name").iri())))

  val Type       = Field(Str, Security + "type", ModelDoc(ModelVocabularies.Security, "type", "Type of security scheme"))
  val CommonType = Field(Str, Security + "commonType", ModelDoc(ModelVocabularies.Security, "Commontype", "Normalized security scheme type"))

  val Headers = Field(Array(ParameterModel),
                      Http + "header",
                      ModelDoc(ModelVocabularies.Http, "header", "Security scheme specific headers"))

  val QueryParameters = Field(Array(ParameterModel),
                              Http + "parameter",
                              ModelDoc(ModelVocabularies.Http, "parameter", "Security scheme specific parameters"))

  val Responses = Field(
    Array(ResponseModel),
    Hydra + "response",
    ModelDoc(ExternalModelVocabularies.Hydra, "response", "Response associated to this security scheme"))

  val Settings = Field(SettingsModel,
                       Security + "settings",
                       ModelDoc(ModelVocabularies.Security, "settings", "Security scheme settings"))

  val QueryString = Field(
    ShapeModel,
    Http + "queryString",
    ModelDoc(ModelVocabularies.Http, "query string", "Query string associated to this security scheme"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Security + "SecurityScheme" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Type, CommonType, DisplayName, Description, Headers, QueryParameters, Responses, Settings, QueryString) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = SecurityScheme()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Security Scheme",
    "Authentication and access control mechanism defined in an API"
  )
}
