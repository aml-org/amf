package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Raml10
import amf.plugins.domain.apicontract.resolution.stages._

class Raml10TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Raml10Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()

}

object Raml10TransformationPipeline {
  def apply()      = new Raml10TransformationPipeline(name)
  val name: String = PipelineName.from(Raml10.mediaType, TransformationPipeline.DEFAULT_PIPELINE)
}
