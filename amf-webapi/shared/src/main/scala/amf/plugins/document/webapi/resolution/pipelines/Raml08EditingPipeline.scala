package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class Raml08EditingPipeline extends AmfEditingPipeline {
  override val parameters                      = new ParametersNormalizationStage(ProfileNames.RAML08)
  override val mediaTypes                      = new MediaTypeResolutionStage(ProfileNames.RAML08)
  override val extensions                      = new ExtensionsResolutionStage(ProfileNames.RAML08, keepEditingInfo = true)
  override val shapes: ShapeNormalizationStage = new ShapeNormalizationStage(ProfileNames.RAML08, keepEditingInfo = true)
}
