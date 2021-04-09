package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Aml, AsyncApi, AsyncApi20, Oas, Oas20, Oas30, Raml, Raml08, Raml10}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance, DialectInstancePatch}
import amf.plugins.document.vocabularies.resolution.pipelines.DefaultAMLTransformationPipeline
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

case class UnifiedDefaultPipeline() extends ResolutionPipeline {

  override def steps(model: BaseUnit, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] = {
    model match {
      case _: Dialect | _: DialectInstance | _: DialectInstancePatch | _
          if (sourceVendor == Amf.name || sourceVendor == Aml.name) =>
        new DefaultAMLTransformationPipeline().steps(model, sourceVendor)
      case _ => apiDefaultPipeline(model, sourceVendor)
    }
  }

  // This is a temporary implementation, ideally we will have a single unified pipeline for api models.
  def apiDefaultPipeline[T <: BaseUnit](model: T, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] =
    sourceVendor match {
      case Raml.name | Raml10.name => new Raml10ResolutionPipeline().steps(model, sourceVendor)
      case Raml08.name             => new Raml08ResolutionPipeline().steps(model, sourceVendor)
      case Oas.name | Oas20.name   => new OasResolutionPipeline().steps(model, sourceVendor)
      case Oas30.name              => new Oas30ResolutionPipeline().steps(model, sourceVendor)
      case AsyncApi.name | AsyncApi20.name =>
        new Async20ResolutionPipeline().steps(model, sourceVendor)
      case _ =>
        errorHandler.violation(ResolutionValidation, "", s"Default pipeline could not handle vendor: $sourceVendor")
        Nil
    }

}
