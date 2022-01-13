package amf.apicontract.internal.transformation.compatibility

import amf.apicontract.internal.transformation.Oas20TransformationPipeline
import amf.apicontract.internal.transformation.compatibility.common.SemanticFlattenFilter
import amf.apicontract.internal.transformation.compatibility.oas._
import amf.apicontract.internal.transformation.compatibility.oas3.{
  CleanRepeatedOperationIds,
  SetValidConsumesForFileParam
}
import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

class Oas20CompatibilityPipeline private (override val name: String)
    extends TransformationPipeline()
    with SemanticFlattenFilter {

  private val baseSteps: Seq[TransformationStep] = filterOutSemanticStage(Oas20TransformationPipeline().steps)

  override def steps: Seq[TransformationStep] =
    baseSteps ++ Seq(
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
