package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf._
import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

class CompatibilityPipeline(targetProfile: ProfileName = RamlProfile) extends ResolutionPipeline() {

  override def steps(model: BaseUnit, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] = targetProfile match {
    case RamlProfile | Raml10Profile | Raml08Profile => new RamlCompatibilityPipeline().steps(model, sourceVendor)
    case Oas30Profile                                => new Oas3CompatibilityPipeline().steps(model, sourceVendor)
    case OasProfile | Oas20Profile                   => new OasCompatibilityPipeline().steps(model, sourceVendor)
    case _ =>
      errorHandler.violation(ResolutionValidation, "", "No compatibility pipeline registered to target profile")
      Nil
  }

}
