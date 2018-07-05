package amf.client.model.document

import amf.client.model.domain.{ArrayNode, ObjectNode, ScalarNode}
import amf.core.model.document.{Fragment => InternalFragment, PayloadFragment => InternalPayloadFragment}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Fragment(private[amf] val _internal: InternalFragment) extends BaseUnit with EncodesModel

@JSExportAll
case class PayloadFragment(override private[amf] val _internal: InternalPayloadFragment) extends Fragment(_internal) {
  @JSExportTopLevel("model.domain.PayloadFragment")
  def this(scalar: ScalarNode, mediaType: String) = this(InternalPayloadFragment(scalar._internal, mediaType))

  @JSExportTopLevel("model.domain.PayloadFragment")
  def this(obj: ObjectNode, mediaType: String) = this(InternalPayloadFragment(obj._internal, mediaType))

  @JSExportTopLevel("model.domain.PayloadFragment")
  def this(arr: ArrayNode, mediaType: String) = this(InternalPayloadFragment(arr._internal, mediaType))
}
