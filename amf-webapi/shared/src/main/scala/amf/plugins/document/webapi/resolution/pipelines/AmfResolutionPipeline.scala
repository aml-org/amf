package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{CleanReferencesStage, DeclarationsRemovalStage, ReferenceResolutionStage}
import amf.plugins.document.webapi.resolution.stages.{ExtendsResolutionStage, ExtensionsResolutionStage}
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, MediaTypeResolutionStage, ParametersNormalizationStage, SecurityResolutionStage}

class AmfResolutionPipeline extends ResolutionPipeline {
  val profileName = ProfileNames.AMF
  val references = new ReferenceResolutionStage(profileName, keepEditingInfo = false)
  val parameters = new ParametersNormalizationStage(profileName)
  val `extends`  = new ExtendsResolutionStage(profileName, keepEditingInfo = false)
  val security   = new SecurityResolutionStage(profileName)
  val examples   = new ExamplesResolutionStage(profileName)
  val extensions = new ExtensionsResolutionStage(profileName, keepEditingInfo = false)
  val cleanRefs  = new CleanReferencesStage(profileName)
  val cleanDecls = new DeclarationsRemovalStage(profileName)

  override def resolve[T <: BaseUnit](model: T): T = {
    ExecutionLog.log(s"AmfResolutonPipeline#resolve: resolving ${model.location}")
    val mediaTypes = new MediaTypeResolutionStage(profileName, errorHandlerForModel(model))
    withModel(model) { () =>
      commonSteps(model)
      step(parameters)
      step(mediaTypes)
      step(examples)
      step(cleanRefs)
      step(cleanDecls)
      ExecutionLog.log(s"AmfResolutonPipeline#resolve: resolution finished ${model.location}")
    }
  }

  protected def commonSteps(model: BaseUnit): Unit = {
    val shapes  = new ShapeNormalizationStage(profileName, keepEditingInfo = false, errorHandlerForModel(model))
    step(references)
    step(extensions)
    step(shapes)
    step(security)
  }
}
