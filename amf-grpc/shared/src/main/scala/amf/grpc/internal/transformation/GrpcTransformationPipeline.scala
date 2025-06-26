package amf.grpc.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.AnnotationRemovalStage
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage}

class GrpcTransformationPipeline private(override val name: String) extends TransformationPipeline() {

  override def steps: Seq[TransformationStep] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new AnnotationRemovalStage()
  ) :+ SourceInformationStage
}

object GrpcTransformationPipeline {
  def apply()      = new GrpcTransformationPipeline(name)
  val name: String = PipelineId.Default
}
