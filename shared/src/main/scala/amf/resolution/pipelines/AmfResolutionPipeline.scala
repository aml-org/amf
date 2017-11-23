package amf.resolution.pipelines

import amf.ProfileNames
import amf.framework.document.BaseUnit
import amf.resolution.stages._

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
