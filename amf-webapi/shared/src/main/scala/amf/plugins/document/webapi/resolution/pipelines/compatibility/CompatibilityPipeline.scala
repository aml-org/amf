package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf._
import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

class CompatibilityPipeline(override val eh: ErrorHandler, targetProfile: ProfileName = RamlProfile)
    extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = profileName match {
    case RamlProfile | Raml10Profile | Raml08Profile => new RamlCompatibilityPipeline(eh).steps
    case Oas30Profile                                => new Oas3CompatibilityPipeline(eh).steps
    case OasProfile | Oas20Profile                   => new OasCompatibilityPipeline(eh).steps
    case _ =>
      eh.violation(ResolutionValidation, "", "No compatibility pipeline registered to target profile")
      Nil
  }

  override def profileName: ProfileName = targetProfile
}

object CompatibilityPipeline {
  def unhandled()                     = new CompatibilityPipeline(UnhandledErrorHandler)
  def unhandled(profile: ProfileName) = new CompatibilityPipeline(UnhandledErrorHandler, profile)
}
