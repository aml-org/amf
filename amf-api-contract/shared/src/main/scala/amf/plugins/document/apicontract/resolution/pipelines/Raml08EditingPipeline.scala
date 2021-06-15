package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Raml08
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}

class Raml08EditingPipeline private (urlShortening: Boolean = true,
                                     override val name: String = Raml08EditingPipeline.name)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Raml08EditingPipeline {

  def apply()                    = new Raml08EditingPipeline(true, name)
  private[amf] def cachePipeline = new Raml08EditingPipeline(false, Raml08CachePipeline.name)
  val name: String               = PipelineName.from(Raml08.name, TransformationPipeline.EDITING_PIPELINE)
}

object Raml08CachePipeline {
  def apply(): Raml08EditingPipeline = Raml08EditingPipeline.cachePipeline
  val name: String                   = PipelineName.from(Raml08.name, TransformationPipeline.COMPATIBILITY_PIPELINE)
}
