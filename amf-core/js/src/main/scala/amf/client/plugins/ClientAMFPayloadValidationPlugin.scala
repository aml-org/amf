package amf.client.plugins

import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.client.validate.{ValidationReport, ValidationShapeSet}

import scala.scalajs.js

@js.native
trait ClientAMFPayloadValidationPlugin extends ClientAMFPlugin {

  def parsePayload(payload: String, mediaType: String, env: Environment, shape: Shape): PayloadFragment = js.native

  def validateSet(set: ValidationShapeSet, env: Environment): ClientFuture[ValidationReport] = js.native

  val payloadMediaType: ClientList[String] = js.native

  def canValidate(shape: Shape, env: Environment): Boolean = js.native
}
