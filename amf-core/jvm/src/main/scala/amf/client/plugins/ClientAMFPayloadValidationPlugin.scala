package amf.client.plugins

import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.client.validate.{ValidationReport, ValidationShapeSet}

import amf.client.convert.CoreClientConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait ClientAMFPayloadValidationPlugin extends ClientAMFPlugin {

  def parsePayload(payload: String, mediaType: String): PayloadFragment

  def validateSet(set: ValidationShapeSet): ClientFuture[ValidationReport]

  val payloadMediaType: ClientList[String]

  def canValidate(shape: Shape): Boolean
}
