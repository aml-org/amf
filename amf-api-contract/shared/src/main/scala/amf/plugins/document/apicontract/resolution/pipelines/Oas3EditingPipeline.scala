package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{Oas30Profile, ProfileName}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.remote.Oas30
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas3EditingPipeline private(urlShortening: Boolean, override val name:String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = Seq(
    RequestParamsLinkStage,
  ) ++ super.steps
}

object Oas3EditingPipeline{
  val name:String = PipelineName.from(Oas30.name, TransformationPipeline.EDITING_PIPELINE)
  def apply() = new Oas3EditingPipeline(true, name = name)

  private[amf] def cachePipeline() = new Oas3EditingPipeline(false, Oas3CachePipeline.name)
}

object Oas3CachePipeline{
  val name:String = PipelineName.from(Oas30.name, TransformationPipeline.CACHE_PIPELINE)
  def apply(): Oas3EditingPipeline = Oas3EditingPipeline.cachePipeline()
}
