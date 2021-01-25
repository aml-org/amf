package amf.client.`new`.amfcore

import amf.ProfileName
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.resolution.stages.selectors.{NodeShapeSelector, Selector}

class AmfResolutionPipeline(profile: ProfileName, steps: List[AmfResolutionStep]) {
  def resolve(bu: BaseUnit): BaseUnit

}

trait AmfResolutionStep {
  def apply(bu: BaseUnit): Boolean

  protected var m: Option[BaseUnit] = None
  def resolve(model: BaseUnit): BaseUnit
}

// field resolution step could have sense?

trait ElementResolutionStep[T <: DomainElement] extends AmfResolutionStep {

  protected val selector: Selector

  override def resolve(model: BaseUnit): BaseUnit = {
    m = Some(model)
    model.transform(selector, transform)
  }

  def transform(element: T, isCycle: Boolean): T
}
