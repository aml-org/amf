package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.Oas30ResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3._
import amf.{Oas30Profile, ProfileName}

class Oas3CompatibilityPipeline() extends ResolutionPipeline() {

  val resolution = new Oas30ResolutionPipeline()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] =
    resolution.steps(eh) ++ Seq(
      new CleanNullSecurity(),
      new CleanSchemes(),
      new MandatoryDocumentationUrl(),
      new MandatoryResponses(),
      new Oas30SecuritySettingsMapper(),
      new MandatoryPathParameters(),
      new AddItemsToArrayType(),
      new CleanRepeatedOperationIds()
    )

}
