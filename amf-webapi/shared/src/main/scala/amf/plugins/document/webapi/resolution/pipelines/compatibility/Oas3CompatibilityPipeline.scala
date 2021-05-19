package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Oas30
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.stages.TransformationStep
import amf.plugins.document.webapi.resolution.pipelines.Oas30TransformationPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3._

class Oas3CompatibilityPipeline private (override val name: String) extends TransformationPipeline() {

  val resolution = Oas30TransformationPipeline()

  override def steps: Seq[TransformationStep] =
    resolution.steps ++ Seq(
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

object Oas3CompatibilityPipeline {
  def apply(): Oas3CompatibilityPipeline = new Oas3CompatibilityPipeline(name)
  val name: String                       = PipelineName.from(Oas30.name, TransformationPipeline.COMPATIBILITY_PIPELINE)
}
