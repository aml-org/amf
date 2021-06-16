package amf.apicontract.internal.metamodel.document

import amf.apicontract.client.scala.model.document.Overlay
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

/**
  * An overlay adds or overrides nodes of a RAML API definition while preserving its behavioral, functional aspects.
  * Certain nodes of a RAML API definition specify the behavior of an API: its resources, methods, parameters, bodies, responses, and so on.
  * These nodes cannot be changed by applying an overlay. In contrast, other nodes, such as descriptions or annotations,
  * address concerns beyond the functional interface, such as the human-oriented descriptive documentation in some language,
  * or implementation or verification information for use in automated tools. These nodes can be changed by applying an overlay.
  */
object OverlayModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(ApiContract + "Overlay") ++ DocumentModel.`type`

  override def modelInstance = Overlay()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Overlay",
    "Model defining a RAML overlay"
  )
}
