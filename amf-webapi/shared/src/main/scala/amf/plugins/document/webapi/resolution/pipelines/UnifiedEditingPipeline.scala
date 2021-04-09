package amf.plugins.document.webapi.resolution.pipelines

import amf.{AmfProfile, ProfileName}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Aml, AsyncApi, AsyncApi20, Oas, Oas20, Oas30, Raml, Raml08, Raml10}
import amf.core.resolution.pipelines.{BasicResolutionPipeline, ResolutionPipeline}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance, DialectInstancePatch}
import amf.plugins.document.vocabularies.resolution.pipelines.DefaultAMLTransformationPipeline
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

case class UnifiedEditingPipeline(urlShortening: Boolean = true) extends ResolutionPipeline {

  override def steps(model: BaseUnit, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] = {
    model match {
      case _: Dialect | _: DialectInstance | _: DialectInstancePatch | _
          if (sourceVendor == Amf.name || sourceVendor == Aml.name) =>
        // hack to maintain compatibility with legacy behaviour, aml does not have editing pipeline and this should be removed.
        new DefaultAMLTransformationPipeline().steps(model, sourceVendor)
      case _ => apiEditingPipeline(model, sourceVendor)
    }
  }

  // This is a temporary implementation, ideally we will have a single unified pipeline for api models.
  def apiEditingPipeline[T <: BaseUnit](model: T, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] =
    sourceVendor match {
      case Raml.name | Raml10.name =>
        new Raml10EditingPipeline(urlShortening).steps(model, sourceVendor)
      case Raml08.name           => new Raml08EditingPipeline(urlShortening).steps(model, sourceVendor)
      case Oas.name | Oas20.name => new OasEditingPipeline(urlShortening).steps(model, sourceVendor)
      case Oas30.name            => new Oas30EditingPipeline(urlShortening).steps(model, sourceVendor)
      case AsyncApi.name | AsyncApi20.name =>
        new Async20EditingPipeline(urlShortening).steps(model, sourceVendor)
      case _ =>
        errorHandler.violation(ResolutionValidation, "", s"Default pipeline could not handle vendor: $sourceVendor")
        Nil
    }

}
