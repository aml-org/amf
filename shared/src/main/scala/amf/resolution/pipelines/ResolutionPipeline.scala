package amf.resolution.pipelines

import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.ResolutionStage

abstract class ResolutionPipeline {

  var model: Option[BaseUnit] = None

  def resolve(model: BaseUnit): BaseUnit

  protected def step(stage: ResolutionStage): Unit = {
    model = Some(stage.resolve(model.get))
  }

  protected def withModel(unit: BaseUnit)(block: () => Unit): BaseUnit = {
    model = Some(unit)
    block()
    model.get
  }

}

object ResolutionPipeline {
  def raml = new RamlResolutionPipeline()
  def oas  = new OasResolutionPipeline()
  def amf  = new AmfResolutionPipeline()

  def forProfile(profile: String): ResolutionPipeline = {
    profile match {
      case ProfileNames.RAML => raml
      case ProfileNames.OAS  => oas
      case ProfileNames.AMF  => amf
      case _                 => throw new Exception(s"Unknown resolution pipeline $profile")
    }
  }
}
