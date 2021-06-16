package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.async.transformation.{AsyncContentTypeResolutionStage, AsyncExamplePropagationResolutionStage, JsonMergePatchStage, ServerVariableExampleResolutionStage}
import amf.apicontract.internal.spec.common.transformation.stage.{AnnotationRemovalStage, PathDescriptionNormalizationStage}
import amf.core.client.common.transform._
import amf.core.client.common.validation.Async20Profile
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.{CleanReferencesStage, DeclarationsRemovalStage, ExternalSourceRemovalStage, TransformationStep}
import amf.core.internal.remote.AsyncApi20
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.apicontract.resolution.stages.async.ServerVariableExampleResolutionStage
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

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
  val name: String = PipelineName.from(AsyncApi20.mediaType, PipelineId.Default)
}
