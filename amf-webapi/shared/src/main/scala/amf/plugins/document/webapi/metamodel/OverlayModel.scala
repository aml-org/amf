package amf.plugins.document.webapi.metamodel

import amf.core.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.plugins.document.webapi.model.Overlay
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * An overlay adds or overrides nodes of a RAML API definition while preserving its behavioral, functional aspects.
  * Certain nodes of a RAML API definition specify the behavior of an API: its resources, methods, parameters, bodies, responses, and so on.
  * These nodes cannot be changed by applying an overlay. In contrast, other nodes, such as descriptions or annotations,
  * address concerns beyond the functional interface, such as the human-oriented descriptive documentation in some language,
  * or implementation or verification information for use in automated tools. These nodes can be changed by applying an overlay.
  */
object OverlayModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Overlay") ++ DocumentModel.`type`

  override def modelInstance = Overlay()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Overlay Model",
    "Model defining a RAML overlay"
  )
}
