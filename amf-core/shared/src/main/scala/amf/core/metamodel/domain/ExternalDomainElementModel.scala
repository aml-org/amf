package amf.core.metamodel.domain

import amf.core.metamodel.{Field, domain}
import amf.core.metamodel.Type.Str
import amf.core.model.domain.ExternalDomainElement
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

/**
  * Domain element containing foreign information that cannot be included into the model semantics
  */
object ExternalDomainElementModel extends DomainElementModel {

  /**
    * Raw textual information that cannot be processed for the current model semantics.
    */
  val Raw = Field(Str, Document + "raw", ModelDoc(ModelVocabularies.AmlDoc, "raw", "Raw textual information that cannot be processed for the current model semantics."))

  val MediaType = Field(Str, Http + "mediaType", ModelDoc(ModelVocabularies.Http, "mediaType", "Media type associated to the encoded fragment information"))

  override def fields: List[Field] = List(Raw, MediaType)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`

  override def modelInstance = ExternalDomainElement()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "External Domain Element",
    "Domain element containing foreign information that cannot be included into the model semantics"
  )
}
