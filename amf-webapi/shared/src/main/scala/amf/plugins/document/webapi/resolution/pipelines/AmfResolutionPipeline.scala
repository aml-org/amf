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

  val references = new ReferenceResolutionStage(ProfileNames.AMF, keepEditingInfo = false)
  val shapes     = new ShapeNormalizationStage(ProfileNames.AMF, keepEditingInfo = false)
  val parameters = new ParametersNormalizationStage(ProfileNames.AMF)
  val `extends`  = new ExtendsResolutionStage(ProfileNames.AMF, keepEditingInfo = false)
  val security   = new SecurityResolutionStage(ProfileNames.AMF)
  val mediaTypes = new MediaTypeResolutionStage(ProfileNames.AMF)
  val examples   = new ExamplesResolutionStage(ProfileNames.AMF)
  val extensions = new ExtensionsResolutionStage(ProfileNames.AMF, keepEditingInfo = false)
  val cleanRefs  = new CleanReferencesStage(ProfileNames.AMF)
  val cleanDecls = new DeclarationsRemovalStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    ExecutionLog.log(s"AmfResolutonPipeline#resolve: resolving ${model.location}")
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
      step(examples)
      step(cleanRefs)
      step(cleanDecls)
      ExecutionLog.log(s"AmfResolutonPipeline#resolve: resolution finished ${model.location}")
    }
  }

  protected def commonSteps(): Unit = {
    step(references)
    step(extensions)
    step(shapes)
    step(security)
  }
}
