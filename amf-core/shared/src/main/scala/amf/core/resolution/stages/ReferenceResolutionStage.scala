package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import org.mulesoft.common.core.Strings

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

  def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = element match {

    // link not traversed, cache it and traverse it
    case l: Linkable if l.linkTarget.isDefined && !isCycle => Some(withName(resolveLinked(l.linkTarget.get), l))

    // link traversed, return the link
    case l: Linkable if l.linkTarget.isDefined => Some(l)

    // no link
    case other => Some(other)
  }

  def withName(resolved: DomainElement, source: DomainElement): DomainElement = {
    resolved match {
      case r: NamedDomainElement if r.name.notNull.isEmpty =>
        source match {
          case s: NamedDomainElement if s.name.notNull.nonEmpty => r.withName(s.name)
          case _                                                =>
        }
      case _ =>
    }
    resolved
  }

  def resolveLinked(element: DomainElement): DomainElement = {
    val nested = Document()
    nested.fields.setWithoutId(DocumentModel.Encodes, element)
    val result = new ReferenceResolutionStage(profile).resolve(nested)
    result.asInstanceOf[Document].encodes
  }
}
