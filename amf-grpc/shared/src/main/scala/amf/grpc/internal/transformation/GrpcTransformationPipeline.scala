package amf.grpc.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.AnnotationRemovalStage
import amf.core.client.common.validation.GrpcProfile
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.ReferenceResolutionStage

class GrpcTransformationPipeline() extends TransformationPipeline() {
  override val name: String = GrpcProfile.profile

  override def steps: Seq[TransformationStep] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new AnnotationRemovalStage()
  )
}

object GrpcTransformationPipeline {
  def apply() = new GrpcTransformationPipeline()
}
