package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.{ShapeNormalizationStage, TypeAliasTransformationStage}
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, RamlProfile}

class Raml10EditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = RamlProfile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage = new Raml10ParametersNormalizationStage()

  override lazy val steps: Seq[ResolutionStage] = Seq(
    new TypeAliasTransformationStage(),
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    new SecurityResolutionStage(),
    parameterNormalizationStage,
    new ServersNormalizationStage(profileName, keepEditingInfo = true),
    new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
    new MediaTypeResolutionStage(profileName, keepEditingInfo = true),
    new ResponseExamplesResolutionStage(),
    new PayloadAndParameterResolutionStage(profileName)
  ) ++ url
}
