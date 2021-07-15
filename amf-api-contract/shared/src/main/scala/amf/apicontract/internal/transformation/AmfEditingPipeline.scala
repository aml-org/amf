package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage._
import amf.apicontract.internal.transformation.stages.{ExtensionsResolutionStage, WebApiReferenceResolutionStage}
import amf.core.client.common.transform._
import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.UrlShortenerStage
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

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
