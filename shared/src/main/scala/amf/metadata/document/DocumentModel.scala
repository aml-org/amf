package amf.metadata.document

import amf.metadata.Field
import amf.metadata.Type.Iri
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Document metamodel
  */
trait DocumentModel extends FragmentModel with ModuleModel {

  override val `type`: List[ValueType] =
    Document + "Document" :: Document + "Fragment" :: Document + "Module" :: BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: Declares :: BaseUnitModel.fields
}

object DocumentModel extends DocumentModel

trait ExtensionLikeModel extends DocumentModel {
  val Extends = Field(Iri, Document + "extends")

  override def fields: List[Field] = Extends :: DocumentModel.fields
}

object ExtensionLikeModel extends ExtensionLikeModel

object ExtensionModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Extension") ++ DocumentModel.`type`
}

object OverlayModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Overlay") ++ DocumentModel.`type`
}