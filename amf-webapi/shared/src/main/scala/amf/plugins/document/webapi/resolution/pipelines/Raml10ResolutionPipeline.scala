package amf.plugins.document.webapi.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Raml10
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, Raml10Profile}

class Raml10ResolutionPipeline private (override val name: String) extends AmfResolutionPipeline(name) {
  override def profileName: ProfileName = Raml10Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()

}

object Raml10ResolutionPipeline {
  def apply()      = new Raml10ResolutionPipeline(name)
  val name: String = PipelineName.from(Raml10.name, ResolutionPipeline.DEFAULT_PIPELINE)
}
