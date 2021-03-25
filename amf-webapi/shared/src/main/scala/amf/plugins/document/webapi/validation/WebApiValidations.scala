package amf.plugins.document.webapi.validation

import amf._
import amf.client.execution.BaseExecutionEnvironment
import amf.client.remod.amfcore.plugins.validate.ValidationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.validation._
import amf.core.validation.core.{ValidationProfile, ValidationResult, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.document.webapi.validation.runner.{ValidationContext, WebApiValidationsRunner}

import scala.concurrent.Future

trait WebApiValidations extends ValidationResultProcessor {

  var aggregatedReport: List[AMFValidationResult] = List()

  val defaultValidationProfiles: Map[String, () => ValidationProfile] =
    DefaultAMFValidations.profiles().foldLeft(Map[String, () => ValidationProfile]()) {
      case (acc, profile) =>
        acc.updated(profile.name.profile, { () =>
          profile
        })
    }

  protected def validationRequestsForBaseUnit(unit: BaseUnit,
                                              profile: ProfileName,
                                              validations: EffectiveValidations,
                                              messageStyle: MessageStyle,
                                              platform: Platform,
                                              env: Environment,
                                              resolved: Boolean,
                                              exec: BaseExecutionEnvironment): Future[AMFValidationReport] = {

    // Before validating we need to resolve to get all the model information
    val plugins = Seq(ParserValidatePlugin, ModelValidatePlugin, ExampleValidatePlugin)
    RemodValidationRunnerFactory
      .build(plugins, new ValidationOptions(profile, env, validations), resolved)
      .run(unit)(exec.executionContext)
  }
}
