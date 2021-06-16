package amf.apicontract.internal.metamodel.document

import amf.apicontract.client.scala.model.document.Extension
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

/**
  * An extension broadens a RAML API definition by adding to, or modifying aspects of its behavior and other functionality.
  * An extension can be useful in separating a core, broadly-available API from layers of functionality available to
  * more restricted audiences, for creating variants of an API for somewhat different purposes, or for specifying instance-specific nodes of an API, such as its service endpoint (URL) without altering its pure interface definition document.
  */
object ExtensionModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(ApiContract + "Extension") ++ DocumentModel.`type`
  override def modelInstance           = Extension()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Extension",
    "API spec information designed to be applied and compelement the information of a base specification. RAML extensions and overlays are examples of extensions."
  )
}
