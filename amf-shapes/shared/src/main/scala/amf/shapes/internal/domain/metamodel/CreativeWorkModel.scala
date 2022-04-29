package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Iri, Str}
import amf.core.internal.metamodel.domain.common.DescriptionField
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.CreativeWork

/** Creative work metamodel
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
    "CreativeWork",
    "The most generic kind of creative work, including books, movies, photographs, software programs, etc."
  )
}
