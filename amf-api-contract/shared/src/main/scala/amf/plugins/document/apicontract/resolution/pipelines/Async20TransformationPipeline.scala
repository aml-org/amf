package amf.plugins.document.apicontract.resolution.pipelines

import amf.Async20Profile
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.AsyncApi20
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  TransformationStep
}
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.apicontract.resolution.stages.async.{
  AsyncContentTypeResolutionStage,
  AsyncExamplePropagationResolutionStage,
  ServerVariableExampleResolutionStage
}

class Async20TransformationPipeline private (override val name: String) extends TransformationPipeline() {
  def references = new WebApiReferenceResolutionStage()

  override def steps: Seq[TransformationStep] =
    Seq(
      references,
      new ExternalSourceRemovalStage,
      new ShapeNormalizationStage(Async20Profile, keepEditingInfo = false),
      new JsonMergePatchStage(isEditing = false),
      new AsyncContentTypeResolutionStage(),
      new AsyncExamplePropagationResolutionStage(),
      new ServerVariableExampleResolutionStage(),
      new PathDescriptionNormalizationStage(Async20Profile),
      new CleanReferencesStage(),
      new DeclarationsRemovalStage(),
      new AnnotationRemovalStage()
    )
}

object Async20TransformationPipeline {
  def apply()      = new Async20TransformationPipeline(name)
  val name: String = PipelineName.from(AsyncApi20.name, TransformationPipeline.DEFAULT_PIPELINE)
}
