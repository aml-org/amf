package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage,
  RequestParamsLinkStage
}
import amf.apicontract.internal.transformation.stages.WebApiReferenceResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas30Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas3EditingPipeline private[amf] (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = RequestParamsLinkStage +: super.steps
}

object Oas3EditingPipeline {
  val name: String = PipelineId.Editing
  def apply()      = new Oas3EditingPipeline(true, name = name)

  private[amf] def cachePipeline() = new Oas3EditingPipeline(false, Oas3CachePipeline.name)
}

object Oas3CachePipeline {
  val name: String                 = PipelineId.Cache
  def apply(): Oas3EditingPipeline = Oas3EditingPipeline.cachePipeline()
}
