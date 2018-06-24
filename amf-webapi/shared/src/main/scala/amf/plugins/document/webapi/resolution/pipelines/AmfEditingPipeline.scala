package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage,
  SecurityResolutionStage
}

class AmfEditingPipeline(override val model: BaseUnit) extends ResolutionPipeline[BaseUnit] {

  override def profileName: ProfileName = ProfileNames.AMF

  val references = new ReferenceResolutionStage(keepEditingInfo = true)

  override protected lazy val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    //    new ExtendsResolutionStage(profileName, keepEditingInfo = true, errorHandler = errorHandler),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new MediaTypeResolutionStage(profileName),
    new ExamplesResolutionStage()
  )

  val ID: String = "editing"
}
