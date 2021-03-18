package amf.plugins.document.webapi.validation.remod

import amf.client.remod.amfcore.plugins.validate.{AMFValidatePlugin, ValidationOptions}
import amf.client.remod.amfcore.plugins.{HighPriority, PluginPriority}
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport
import amf.plugins.document.webapi.validation.runner.ValidationContext
import amf.plugins.document.webapi.validation.runner.steps.{
  ExamplesValidationStep,
  ModelValidationStep,
  ParserValidationStep
}

import scala.concurrent.{ExecutionContext, Future}

object ModelValidatePlugin extends AMFValidatePlugin {

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val context = ValidationContext(unit,
                                    options.profileName,
                                    messageStyle = options.profileName.messageStyle,
                                    validations = options.validations,
                                    env = options.environment)
    ModelValidationStep(context).run
  }

  override val id: String = "somethingAnother"

  override def applies(element: BaseUnit): Boolean = true

  override def priority: PluginPriority = HighPriority
}

object PayloadValidatePlugin extends AMFValidatePlugin {

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val context = ValidationContext(unit,
                                    options.profileName,
                                    messageStyle = options.profileName.messageStyle,
                                    validations = options.validations,
                                    env = options.environment)
    ExamplesValidationStep(context).run
  }

  override val id: String = "somethingElse"

  override def applies(element: BaseUnit): Boolean = true

  override def priority: PluginPriority = HighPriority
}

object ParserValidatePlugin extends AMFValidatePlugin {

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val context = ValidationContext(unit,
                                    options.profileName,
                                    messageStyle = options.profileName.messageStyle,
                                    validations = options.validations,
                                    env = options.environment)
    ParserValidationStep(context).run
  }

  override val id: String = "something" // TODO: change

  override def applies(element: BaseUnit): Boolean = true // TODO: change

  override def priority: PluginPriority = HighPriority
}
