package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.document.webapi.resolution.stages.{ExtendsResolutionStage, ExtensionsResolutionStage}
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, MediaTypeResolutionStage, ParametersNormalizationStage, SecurityResolutionStage}

class AmfResolutionPipeline extends ResolutionPipeline {

  val references = new ReferenceResolutionStage(ProfileNames.AMF)
  val shapes     = new ShapeNormalizationStage(ProfileNames.AMF)
  val parameters = new ParametersNormalizationStage(ProfileNames.AMF)
  val `extends`  = new ExtendsResolutionStage(ProfileNames.AMF)
  val security   = new SecurityResolutionStage(ProfileNames.AMF)
  val mediaTypes = new MediaTypeResolutionStage(ProfileNames.AMF)
  val examples   = new ExamplesResolutionStage(ProfileNames.AMF)
  val extensions = new ExtensionsResolutionStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
      step(examples)
    }
  }

  protected def commonSteps(): Unit = {
    step(references)
    step(extensions)
    step(shapes)
    step(security)
  }
}
