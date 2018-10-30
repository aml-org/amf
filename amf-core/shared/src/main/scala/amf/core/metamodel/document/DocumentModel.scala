package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Document metamodel
  *
  * A Document is a parsing Unit that encodes a stand-alone DomainElement and can include references to other DomainElements that reference from the encoded DomainElement.
  * Since it encodes a DomainElement, but also declares references, it behaves like a Fragment and a Module at the same time.
  * The main difference is that the Document encoded DomainElement is stand-alone and that the references declared are supposed to be private not for re-use from other Units
  */
trait DocumentModel extends FragmentModel with ModuleModel {

  override val `type`: List[ValueType] =
    Document + "Document" :: Document + "Fragment" :: Document + "Module" :: BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: Declares :: BaseUnitModel.fields
}

object DocumentModel extends DocumentModel {
  override def modelInstance = amf.core.model.document.Document()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Document",
    "A Document is a parsing Unit that encodes a stand-alone DomainElement and can include references to other DomainElements that reference from the encoded DomainElement.\nSince it encodes a DomainElement, but also declares references, it behaves like a Fragment and a Module at the same time.\nThe main difference is that the Document encoded DomainElement is stand-alone and that the references declared are supposed to be private not for re-use from other Units"
  )
}







