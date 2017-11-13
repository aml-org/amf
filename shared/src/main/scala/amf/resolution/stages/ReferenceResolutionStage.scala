package amf.resolution.stages

import amf.document.BaseUnit
import amf.domain.{DomainElement, Linkable}
import amf.validation.Validation

/**
  * Resolves the local and remote references found in the model.
  */
class ReferenceResolutionStage(profile: String)(override implicit val currentValidation: Validation) extends ResolutionStage(profile) {

  override def resolve(model: BaseUnit): BaseUnit = {
    model.transform(findLinkPredicates, transform)
  }

  def findLinkPredicates(element: DomainElement): Boolean = {
    element match {
      case l: Linkable => l.isLink
      case _           => false
    }
  }

  def transform(element: DomainElement): Option[DomainElement] = element match {
    case l: Linkable => l.linkTarget
    case other       => Some(other)
  }
}
