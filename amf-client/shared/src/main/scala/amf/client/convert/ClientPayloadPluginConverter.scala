package amf.client.convert

import amf.client.convert.WebApiClientConverters._
import amf.client.plugins._
import amf.core.model.document.{PayloadFragment => InternalPayloadFragment}
import amf.client.plugins.{PayloadParsingResult => InternalPayloadParsingResult}
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, ValidationShapeSet => InternalValidationShapeSet}
import amf.internal.environment.Environment
import amf.client.environment.{Environment => ClientEnvironment}

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
      override protected def parsePayload(payload: String,
                                          mediaType: String,
                                          env: Environment,
                                          shape: Shape): InternalPayloadFragment =
        clientPlugin.parsePayload(payload, mediaType, ClientEnvironment(env), ShapeMatcher.asClient(shape))._internal

      override def parsePayloadWithErrorHandler(payload: String,
                                                mediaType: String,
                                                env: Environment,
                                                shape: Shape): InternalPayloadParsingResult =
        clientPlugin
          .parsePayloadWithErrorHandler(payload, mediaType, ClientEnvironment(env), ShapeMatcher.asClient(shape))
          ._internal

      override def validateSet(set: InternalValidationShapeSet, env: Environment): Future[AMFValidationReport] =
        clientPlugin.validateSet(set, ClientEnvironment(env)).asInternal

      override val payloadMediaType: Seq[String] = clientPlugin.payloadMediaType.asInternal

      override def canValidate(shape: Shape, env: Environment): Boolean =
        clientPlugin.canValidate(ShapeMatcher.asClient(shape), ClientEnvironment(env))

      override val ID: String = clientPlugin.ID

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(clientPlugin.dependencies())(AMFPluginConverter).asInternal

      override def init(): Future[AMFPlugin] = new ClientFutureOps(clientPlugin.init())(AMFPluginConverter).asInternal
    }
}
