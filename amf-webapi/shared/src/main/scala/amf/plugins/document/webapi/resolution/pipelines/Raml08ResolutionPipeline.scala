package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class Raml08ResolutionPipeline extends AmfResolutionPipeline {
  override val parameters                      = new ParametersNormalizationStage(ProfileNames.RAML08)
  override val mediaTypes                      = new MediaTypeResolutionStage(ProfileNames.RAML08)
  override val extensions                      = new ExtensionsResolutionStage(ProfileNames.RAML08, keepEditingInfo = false)
  override val shapes: ShapeNormalizationStage = new ShapeNormalizationStage(ProfileNames.RAML08, keepEditingInfo = false)
}
