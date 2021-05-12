package amf.plugins.domain.webapi.unsafe

import amf.client.execution.BaseExecutionEnvironment
import amf.client.plugins.{StrictValidationMode, ValidationMode}
import amf.core.model.domain.Shape
import amf.internal.environment.Environment
import amf.plugins.document.webapi.validation.remote.PlatformPayloadValidator

trait JsonSchemaSecrets {
  protected def payloadValidator(shape: Shape,
                                 executionEnvironment: BaseExecutionEnvironment): PlatformPayloadValidator =
    JsonSchemaValidatorBuilder.payloadValidator(shape, Environment(executionEnvironment), StrictValidationMode)
}
