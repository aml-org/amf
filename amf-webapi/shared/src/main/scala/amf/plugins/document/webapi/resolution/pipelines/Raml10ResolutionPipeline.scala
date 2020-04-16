package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  ResolutionStage
}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.{ShapeNormalizationStage, TypeAliasTransformationStage}
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, RamlProfile}

class Raml10ResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = RamlProfile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()

  override val steps: scala.Seq[ResolutionStage] = Seq(
    new TypeAliasTransformationStage(),
    references,
    new ExternalSourceRemovalStage,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = false),
    new ShapeNormalizationStage(profileName, keepEditingInfo = false),
    new SecurityResolutionStage(),
    parameterNormalizationStage,
    new ServersNormalizationStage(profileName),
    new PathDescriptionNormalizationStage(profileName),
    new MediaTypeResolutionStage(profileName),
    new ResponseExamplesResolutionStage(),
    new PayloadAndParameterResolutionStage(profileName),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage()
  )
}
