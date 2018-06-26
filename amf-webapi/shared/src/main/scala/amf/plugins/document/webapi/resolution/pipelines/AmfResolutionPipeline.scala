package amf.plugins.document.webapi.resolution.pipelines

import amf.{AMFProfile, ProfileName}
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ReferenceResolutionStage,
  ResolutionStage
}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage,
  SecurityResolutionStage
}

class AmfResolutionPipeline(override val model: BaseUnit) extends ResolutionPipeline[BaseUnit] {
  override def profileName: ProfileName = AMFProfile

  protected lazy val references = new ReferenceResolutionStage(keepEditingInfo = false)

  override protected val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = false),
    new ShapeNormalizationStage(profileName, keepEditingInfo = false),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new MediaTypeResolutionStage(profileName),
    new ExamplesResolutionStage(),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage()
  )

}
