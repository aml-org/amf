package amf.core.plugins

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.AMFValidationReport

import scala.concurrent.Future

trait AMFPayloadValidationPlugin extends AMFPlugin {

  def validatePayload(shape: Shape, payload: String, mediaType: String): Future[AMFValidationReport]

  def validatePayload(shape: Shape, payloadFragment: PayloadFragment): Future[AMFValidationReport]

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape): Boolean
}
