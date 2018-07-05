package amf.client.convert

import amf.client.convert.WebApiClientConverters._
import amf.client.plugins.{AMFPayloadValidationPlugin, AMFPlugin, ClientAMFPayloadValidationPlugin, ClientAMFPlugin}
import amf.core.model.document.{PayloadFragment => InternalPayloadFragment}
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, ValidationShapeSet => InternalValidationShapeSet}

import scala.concurrent.Future
object ClientPayloadPluginConverter {

  implicit object AMFPluginConverter extends ClientInternalMatcher[ClientAMFPlugin, AMFPlugin] {

    override def asInternal(from: ClientAMFPlugin): AMFPlugin = new AMFPlugin {
      override def init(): Future[AMFPlugin] = new ClientFutureOps(from.init())(AMFPluginConverter).asInternal

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(from.dependencies())(AMFPluginConverter).asInternal

      override val ID: String = from.ID
    }
  }

  def convert(clientPlugin: ClientAMFPayloadValidationPlugin): AMFPayloadValidationPlugin =
    new AMFPayloadValidationPlugin {
      override protected def parsePayload(payload: String, mediaType: String): InternalPayloadFragment =
        clientPlugin.parsePayload(payload, mediaType)._internal

      override def validateSet(set: InternalValidationShapeSet): Future[AMFValidationReport] =
        clientPlugin.validateSet(set).asInternal

      override val payloadMediaType: Seq[String] = clientPlugin.payloadMediaType.asInternal

      override def canValidate(shape: Shape): Boolean = clientPlugin.canValidate(ShapeMatcher.asClient(shape))

      override val ID: String = clientPlugin.ID

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(clientPlugin.dependencies())(AMFPluginConverter).asInternal

      override def init(): Future[AMFPlugin] = new ClientFutureOps(clientPlugin.init())(AMFPluginConverter).asInternal
    }
}
