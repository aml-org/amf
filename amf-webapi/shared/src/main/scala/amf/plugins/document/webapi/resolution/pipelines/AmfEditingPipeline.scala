package amf.plugins.document.webapi.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage, UrlShortenerStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage,
  SecurityResolutionStage
}
import amf.{AmfProfile, ProfileName}

class AmfEditingPipeline(override val model: BaseUnit) extends ResolutionPipeline[BaseUnit] {

  override def profileName: ProfileName = AmfProfile

  val references = new ReferenceResolutionStage(keepEditingInfo = true)

  override protected lazy val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    //    new ExtendsResolutionStage(profileName, keepEditingInfo = true, errorHandler = errorHandler),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new MediaTypeResolutionStage(profileName),
    new ExamplesResolutionStage(),
    new UrlShortenerStage()
  )

  val ID: String = "editing"
}
