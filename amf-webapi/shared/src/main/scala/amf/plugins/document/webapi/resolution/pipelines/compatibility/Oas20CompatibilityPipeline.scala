package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Oas20
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.Oas20ResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3.CleanRepeatedOperationIds

class Oas20CompatibilityPipeline private (override val name: String) extends ResolutionPipeline() {

  private val resolution = Oas20ResolutionPipeline()

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

object Oas20CompatibilityPipeline {
  def apply(): Oas20CompatibilityPipeline = new Oas20CompatibilityPipeline(name)
  val name: String                        = PipelineName.from(Oas20.name, ResolutionPipeline.COMPATIBILITY_PIPELINE)
}
