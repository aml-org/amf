package amf.plugins.document.apicontract.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Raml10
import amf.core.resolution.pipelines.TransformationPipeline
import amf.plugins.domain.apicontract.resolution.stages._
import amf.{ProfileName, Raml10Profile}

class Raml10TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Raml10Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()

}

object Raml10TransformationPipeline {
  def apply()      = new Raml10TransformationPipeline(name)
  val name: String = PipelineName.from(Raml10.name, TransformationPipeline.DEFAULT_PIPELINE)
}
