package amf.plugins.document.webapi.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Raml10
import amf.core.resolution.pipelines.TransformationPipeline
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, Raml10Profile}

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
