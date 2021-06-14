package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Raml10
import amf.plugins.domain.apicontract.resolution.stages._

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
  val name: String                 = PipelineName.from(Raml10.name, TransformationPipeline.EDITING_PIPELINE)
}

object Raml10CachePipeline {
  def apply(): Raml10EditingPipeline = Raml10EditingPipeline.cachePipeline()
  val name: String                   = PipelineName.from(Raml10.name, TransformationPipeline.CACHE_PIPELINE)
}
