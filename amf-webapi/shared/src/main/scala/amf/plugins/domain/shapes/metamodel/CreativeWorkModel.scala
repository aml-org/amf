package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.Schema
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.CreativeWork

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel with LinkableElementModel with DescriptionField {

  val Url = Field(Iri, Schema + "url", ModelDoc(ExternalModelVocabularies.SchemaOrg, "url", "URL for the creative work"))

  val Title = Field(Str, Schema + "title", ModelDoc(ExternalModelVocabularies.SchemaOrg, "title", "Title of the creative work"))

  override val `type`: List[ValueType] = Schema + "CreativeWork" :: DomainElementModel.`type`

  override def fields: List[Field] =
    Url :: Title :: Description :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = CreativeWork()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Creative Work",
    "he most generic kind of creative work, including books, movies, photographs, software programs, etc."
  )
}
