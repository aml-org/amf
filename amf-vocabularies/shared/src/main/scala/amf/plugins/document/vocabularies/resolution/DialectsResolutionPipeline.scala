package amf.plugins.document.vocabularies.resolution

import amf.ProfileNames
import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage

/**
  * Created by kor on 19/01/2018.
  */
class DialectsResolutionPipeline extends  ResolutionPipeline{

  val references = new ReferenceResolutionStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    model.fields.remove(BaseUnitModel.DescribedBy)
    withModel(model) { () =>
      commonSteps()
    }
  }
  protected def commonSteps(): Unit = {
    step(references)
  }
}
