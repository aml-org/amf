package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.common.transform._
import amf.core.client.scala.transform.stages.{TransformationStep, UrlShortenerStage}
import amf.plugins.document.apicontract.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage

class AmfEditingPipeline private[amf] (urlShortening: Boolean = true, override val name: String)
    extends TransformationPipeline() {

  protected def references = new WebApiReferenceResolutionStage(keepEditingInfo = true)
  protected def url: Option[UrlShortenerStage] =
    if (urlShortening) Some(new UrlShortenerStage()) else None

  protected def parameterNormalizationStage: ParametersNormalizationStage =
    new AmfParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = {
    Seq(
      references,
      new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
      new ShapeNormalizationStage(profileName, keepEditingInfo = true),
      new SecurityResolutionStage(),
      parameterNormalizationStage,
      new ServersNormalizationStage(profileName, keepEditingInfo = true),
      new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
      new MediaTypeResolutionStage(profileName, keepEditingInfo = true),
      new ResponseExamplesResolutionStage(),
      new PayloadAndParameterResolutionStage(profileName),
      new AnnotationRemovalStage()
    ) ++ url
  }

  val ID: String               = PipelineId.Editing
  def profileName: ProfileName = AmfProfile
}

object AmfEditingPipeline {
  val name: String = PipelineId.Editing
  def apply()      = new AmfEditingPipeline(name = name)
}
