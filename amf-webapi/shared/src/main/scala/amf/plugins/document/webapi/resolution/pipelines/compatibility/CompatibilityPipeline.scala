package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf._

class CompatibilityPipeline(override val eh: ErrorHandler, targetProfile: ProfileName = RamlProfile)
    extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = profileName match {
    case RamlProfile | Raml10Profile | Raml08Profile => new OAStoRAMLCompatibilityPipeline(eh).steps
    case OasProfile | Oas20Profile | Oas30Profile    => new RAMLtoOASCompatibilityPipeline(eh).steps
    case _                                           => Nil
  }

  override def profileName: ProfileName = targetProfile
}

object CompatibilityPipeline {
  def unhandled()                     = new CompatibilityPipeline(UnhandledErrorHandler)
  def unhandled(profile: ProfileName) = new CompatibilityPipeline(UnhandledErrorHandler, profile)
}
