package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3.CleanRepeatedOperationIds
import amf.{OasProfile, ProfileName}

class OasCompatibilityPipeline() extends ResolutionPipeline() {

  private val resolution = new OasResolutionPipeline()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] =
    resolution.steps(eh) ++ Seq(
      new LowercaseSchemes(),
      new Oas20SecuritySettingsMapper(),
      new MandatoryDocumentationUrl(),
      new MandatoryResponses(),
      new MandatoryPathParameters(),
      new CleanNullSecurity(),
      new CleanParameterExamples(),
      new CleanIdenticalExamples(),
      new CleanRepeatedOperationIds()
    )

}
