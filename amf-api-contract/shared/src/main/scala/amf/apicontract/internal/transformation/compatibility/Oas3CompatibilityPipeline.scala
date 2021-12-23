package amf.apicontract.internal.transformation.compatibility

import amf.apicontract.internal.transformation.Oas30TransformationPipeline
import amf.apicontract.internal.transformation.compatibility.common.SemanticFlattenFilter
import amf.apicontract.internal.transformation.compatibility.oas3._
import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

class Oas3CompatibilityPipeline private (override val name: String)
    extends TransformationPipeline()
    with SemanticFlattenFilter {

  private val baseSteps: Seq[TransformationStep] = filterOutSemanticStage(Oas30TransformationPipeline().steps)

  override def steps: Seq[TransformationStep] =
    baseSteps ++ Seq(
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
  val name: String                       = PipelineId.Compatibility
}
