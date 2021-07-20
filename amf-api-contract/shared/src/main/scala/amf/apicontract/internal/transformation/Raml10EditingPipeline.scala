package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{
  ParametersNormalizationStage,
  Raml10ParametersNormalizationStage
}
import amf.apicontract.internal.transformation.stages.WebApiReferenceResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.internal.remote.Raml10

class Raml10EditingPipeline private (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Raml10Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()
}

object Raml10EditingPipeline {
  def apply()                      = new Raml10EditingPipeline(true, name)
  private[amf] def cachePipeline() = new Raml10EditingPipeline(false, Raml10CachePipeline.name)
  val name: String                 = PipelineId.Editing
}

object Raml10CachePipeline {
  def apply(): Raml10EditingPipeline = Raml10EditingPipeline.cachePipeline()
  val name: String                   = PipelineId.Cache
}
