package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.apicontract.internal.transformation.Oas20EditingPipeline.cachePipeline
import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas20Profile, ProfileName}
import amf.core.internal.remote.Oas20
import amf.plugins.domain.apicontract.resolution.stages.OpenApiParametersNormalizationStage

class Oas20EditingPipeline private (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20EditingPipeline {
  def apply()                    = new Oas20EditingPipeline(true, name)
  private[amf] def cachePipeline = new Oas20EditingPipeline(false, Oas20CachePipeline.name)
  val name: String               = PipelineName.from(Oas20.mediaType, PipelineId.Editing)
}

object Oas20CachePipeline {
  def apply(): Oas20EditingPipeline = cachePipeline
  val name: String                  = PipelineName.from(Oas20.mediaType, PipelineId.Cache)
}
