package amf.client.convert

import amf.client.convert.WebApiClientConverters._
import amf.client.environment.{Environment => ClientEnvironment}
import amf.client.plugins._
import amf.core.model.document.{PayloadFragment => InternalPayloadFragment}
import amf.core.model.domain.Shape
import amf.core.validation.{AMFPayloadValidationPlugin, AMFValidationReport, PayloadValidator}
import amf.internal.environment.Environment

import scala.concurrent.{ExecutionContext, Future}
object ClientPayloadPluginConverter {

  implicit object AMFPluginConverter extends ClientInternalMatcher[ClientAMFPlugin, AMFPlugin] {

    override def asInternal(from: ClientAMFPlugin): AMFPlugin = new AMFPlugin {
      override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] =
        new ClientFutureOps(from.init())(AMFPluginConverter, executionContext).asInternal

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(from.dependencies())(AMFPluginConverter).asInternal

      override val ID: String = from.ID
    }
  }

  def convert(clientPlugin: ClientAMFPayloadValidationPlugin): AMFPayloadValidationPlugin =
    new AMFPayloadValidationPlugin {

      override val payloadMediaType: Seq[String] = clientPlugin.payloadMediaType.asInternal

      override def canValidate(shape: Shape, env: Environment): Boolean =
        clientPlugin.canValidate(ShapeMatcher.asClient(shape), ClientEnvironment(env))

      override val ID: String = clientPlugin.ID

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(clientPlugin.dependencies())(AMFPluginConverter).asInternal

      override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] =
        new ClientFutureOps(clientPlugin.init())(AMFPluginConverter, executionContext).asInternal

      override def validator(s: Shape,
                             mediaType: String,
                             env: Environment,
                             validationMode: ValidationMode): PayloadValidator = {
        val validator = clientPlugin.validator(s, ClientEnvironment(env), validationMode)
        new PayloadValidator {
          override def validate(payload: String)(
              implicit executionContext: ExecutionContext): Future[AMFValidationReport] =
            validator.validate(payload).asInternal
          override def validate(payloadFragment: InternalPayloadFragment)(
              implicit executionContext: ExecutionContext): Future[AMFValidationReport] =
            validator.validate(payloadFragment).asInternal
          override def isValid(payload: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
            validator.isValid(payload).asInternal
          override def syncValidate(payload: String): AMFValidationReport =
            validator.syncValidate(payload)
        }
      }
    }
}
