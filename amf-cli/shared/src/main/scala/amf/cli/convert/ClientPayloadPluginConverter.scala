package amf.cli.convert

import amf.client.convert.ApiClientConverters._
import amf.client.convert.ClientInternalMatcher
import amf.client.environment.{DefaultEnvironment, Environment => ClientEnvironment}
import amf.client.plugins._
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
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

      override def canValidate(shape: Shape, config: ValidationConfiguration): Boolean = {
        // TODO ARM change env for conf when is done
        clientPlugin.canValidate(ShapeMatcher.asClient(shape), DefaultEnvironment())
      }

      override val ID: String = clientPlugin.ID

      override def dependencies(): Seq[AMFPlugin] =
        new ClientListOps(clientPlugin.dependencies())(AMFPluginConverter).asInternal

      override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] =
        new ClientFutureOps(clientPlugin.init())(AMFPluginConverter, executionContext).asInternal

      override def validator(s: Shape,
                             config: ValidationConfiguration,
                             validationMode: ValidationMode): PayloadValidator = {
        // TODO ARM change for config interface
        val validator = clientPlugin.validator(s, DefaultEnvironment(), validationMode)
        new PayloadValidator {
          override val shape: Shape                   = validator.shape
          override val defaultSeverity: String        = validator.defaultSeverity
          override val validationMode: ValidationMode = validator.validationMode
          override def validate(payload: String, mediaType: String)(
              implicit executionContext: ExecutionContext): Future[AMFValidationReport] =
            validator.validate(payload, mediaType).asInternal
          override def validate(payloadFragment: InternalPayloadFragment)(
              implicit executionContext: ExecutionContext): Future[AMFValidationReport] =
            validator.validate(payloadFragment).asInternal
          override def isValid(payload: String, mediaType: String)(
              implicit executionContext: ExecutionContext): Future[Boolean] =
            validator.isValid(payload, mediaType).asInternal
          override def syncValidate(mediaType: String, payload: String): AMFValidationReport =
            validator.syncValidate(mediaType, payload)

          // TODO ARM implement client config when is done
          override val configuration: ValidationConfiguration = ???
        }
      }
    }
}
