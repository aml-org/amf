package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.Http
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Encoding

/**
  * Encoding metamodel.
  */
object EncodingModel extends DomainElementModel with KeyField {

  val PropertyName = Field(Str, Http + "propertyName", ModelDoc(ModelVocabularies.Http, "property name", ""))

  val ContentType = Field(Str, Http + "contentType", ModelDoc(ModelVocabularies.Http, "content type", ""))

  val Headers = Field(Array(ParameterModel), Http + "header", ModelDoc(ModelVocabularies.Http, "header", ""))

  val Style = Field(Str, Http + "style", ModelDoc(ModelVocabularies.Http, "style", ""))

  val Explode = Field(Bool, Http + "explode", ModelDoc(ModelVocabularies.Http, "explode", ""))

  val AllowReserved = Field(Bool, Http + "allowReserved", ModelDoc(ModelVocabularies.Http, "allow reserved", ""))

  override val `type`: List[ValueType] = Http + "Encoding" :: DomainElementModel.`type`

  override def fields: List[Field] =
    PropertyName :: ContentType :: Headers :: Style :: Explode :: AllowReserved :: DomainElementModel.fields

  override def modelInstance = Encoding()

  override val key: Field = PropertyName

  // TODO: doc, describe this model
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Encoding",
    ""
  )
}
