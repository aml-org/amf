package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.document.webapi.resolution.stages.{ExtendsResolutionStage, ExtensionsResolutionStage}
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, MediaTypeResolutionStage, ParametersNormalizationStage, SecurityResolutionStage}

class AmfEditingPipeline extends ResolutionPipeline {

  val profileName: String = ProfileNames.AMF

  val references = new ReferenceResolutionStage(profileName, keepEditingInfo = true)
  val parameters = new ParametersNormalizationStage(profileName)
  val `extends`  = new ExtendsResolutionStage(profileName, keepEditingInfo = true)
  val security   = new SecurityResolutionStage(profileName)
  val examples   = new ExamplesResolutionStage(profileName)
  val extensions = new ExtensionsResolutionStage(profileName, keepEditingInfo = true)

  val ID: String = "editing"

  override def resolve[T <: BaseUnit](model: T): T = {
    ExecutionLog.log(s"AmfEditingPipeline#resolve: resolving ${model.location}")
    val mediaTypes = new MediaTypeResolutionStage(profileName, errorHandlerForModel(model))
    withModel(model) { () =>
      commonSteps(model)
      step(parameters)
      step(mediaTypes)
      step(examples)
      ExecutionLog.log(s"AmfEditingPipeline#resolve: resolved model ${model.location}")
    }
  }


  protected def commonSteps(model: BaseUnit): Unit = {
    val shapes     = new ShapeNormalizationStage(profileName, keepEditingInfo = true, errorHandlerForModel(model))
    step(references)
    step(extensions)
    step(shapes)
    step(security)
  }
}
