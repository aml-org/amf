package amf.apicontract.internal.transformation.compatibility

import amf.apicontract.internal.transformation.Oas20TransformationPipeline
import amf.apicontract.internal.transformation.compatibility.oas._
import amf.apicontract.internal.transformation.compatibility.oas3.{CleanRepeatedOperationIds, SetValidConsumesForFileParam}
import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.remote.Oas20

class Oas20CompatibilityPipeline private (override val name: String) extends TransformationPipeline() {

  private val resolution = Oas20TransformationPipeline()

  override def steps: Seq[TransformationStep] =
    resolution.steps ++ Seq(
      new LowercaseSchemes(),
      new Oas20SecuritySettingsMapper(),
      new MandatoryDocumentationUrl(),
      new MandatoryResponses(),
      new MandatoryPathParameters(),
      new CleanNullSecurity(),
      new CleanParameterExamples(),
      new CleanIdenticalExamples(),
      new CleanRepeatedOperationIds(),
      new SetValidConsumesForFileParam()
    )

}

object Oas20CompatibilityPipeline {
  def apply(): Oas20CompatibilityPipeline = new Oas20CompatibilityPipeline(name)
  val name: String                        = PipelineId.Compatibility
}
