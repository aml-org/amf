package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{DomainElement, Linkable}

/**
  * Resolves the local and remote references found in the model.
  */
class ReferenceResolutionStage(profile: String) extends ResolutionStage(profile) {

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
    case l: Linkable if l.linkTarget.isDefined=> {
      Some(resolveLinked(l.linkTarget.get))
    }
    case other       => {
      Some(other)
    }
  }

  def resolveLinked(element: DomainElement): DomainElement = {
    val nested = Document()
    nested.fields.setWithoutId(DocumentModel.Encodes, element)
    val result = new ReferenceResolutionStage(profile).resolve(nested)
    result.asInstanceOf[Document].encodes
  }
}
