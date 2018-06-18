package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * A Document that extends a target document, overwritting part of the information or overlaying additional information.
  */
trait ExtensionLikeModel extends DocumentModel {

  /**
    * Document that is going to be extended overlaying or adding additional information
    */
  val Extends = Field(Iri, Document + "extends")

  override def fields: List[Field] = Extends :: DocumentModel.fields

}

object ExtensionLikeModel extends ExtensionLikeModel {

  override val `type`: List[ValueType] = List(Document + "DocumentExtension") ++ BaseUnitModel.`type`
}