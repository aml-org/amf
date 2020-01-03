package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.Core
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.CreativeWork

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends DomainElementModel with LinkableElementModel with DescriptionField {

  val Url =
    Field(Iri, Core + "url", ModelDoc(ModelVocabularies.Core, "url", "URL for the creative work"))

  val Title =
    Field(Str, Core + "title", ModelDoc(ModelVocabularies.Core, "title", "Title of the item"))

  override val `type`: List[ValueType] = Core + "CreativeWork" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Url :: Title :: Description :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = CreativeWork()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "Creative Work",
    "The most generic kind of creative work, including books, movies, photographs, software programs, etc."
  )
}
