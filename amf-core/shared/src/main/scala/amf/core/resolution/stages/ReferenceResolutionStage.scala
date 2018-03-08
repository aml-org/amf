package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document, EncodesModel}
import amf.core.model.domain._
import org.mulesoft.common.core.Strings

import scala.collection.mutable

/**
  * Class that holds a resolved dynamic link node, with the original link information
  * @param source the link node that has been resolved
  * @param resolved the piece of domain linked through the resolved link node
  */
class ResolvedLinkNode(source: LinkNode, resolved: DomainElement)
// resolved so alias -> value
    extends LinkNode(source.value, source.value, source.fields, source.annotations)

/**
  * Class to store the mapping of named assigned to the linked entity when resolved.
  * We cannot just overwrite name because that would be overwritten in every single place
  * where the entity has been included
  * @param vals map of names and named entities
  */
case class ResolvedNamedEntity(vals: mutable.HashMap[String, Seq[NamedDomainElement]] = mutable.HashMap())
    extends Annotation

class ModelReferenceResolver(model: BaseUnit) {

  def findFragment(url: String): Option[DomainElement] = {
    model match {
      case encodes: EncodesModel if model.location == url => Some(encodes.encodes)
      case _ if model.location == url                     => None
      case _ =>
        var remaining                     = model.references.map(new ModelReferenceResolver(_))
        var result: Option[DomainElement] = None
        while (remaining.nonEmpty) {
          remaining.head.findFragment(url) match {
            case res: Some[DomainElement] =>
              result = res
              remaining = Nil
            case _ => remaining = remaining.tail
          }
        }
        result
    }
  }
}

/**
  * Resolves the local and remote references found in the model.
  */
class ReferenceResolutionStage(profile: String) extends ResolutionStage(profile) {

  var mutuallyRecursive: Seq[String]                = Nil
  var model: Option[BaseUnit]                       = None
  var modelResolver: Option[ModelReferenceResolver] = None

  override def resolve(model: BaseUnit): BaseUnit = {
    this.model = Some(model)
    this.modelResolver = Some(new ModelReferenceResolver(model))
    model.transform(findLinkPredicates, transform)
  }

  // Internal request that checks for mutually recursive types
  protected def recursiveResolveInvocation(model: BaseUnit,
                                           modelResolver: Option[ModelReferenceResolver],
                                           mutuallyRecursive: Seq[String]): BaseUnit = {
    this.mutuallyRecursive = mutuallyRecursive
    this.model = Some(model)
    this.modelResolver = Some(modelResolver.getOrElse(new ModelReferenceResolver(model)))
    model.transform(findLinkPredicates, transform)
  }

  def findLinkPredicates(element: DomainElement): Boolean =
    element match {
      case l: Linkable => l.isLink

      // link in a data node (trait or resource type) see DataNodeParser for details
      case l: LinkNode => true

      case _ => false
    }

  def resolveDynamicLink(l: LinkNode): Option[DomainElement] = {
    modelResolver.get.findFragment(l.value) match {
      case Some(elem) => Some(new ResolvedLinkNode(l, elem))
      case _          => Some(l)
    }
  }

  def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {

      // link not traversed, cache it and traverse it
      case l: Linkable if l.linkTarget.isDefined && !isCycle => Some(withName(resolveLinked(l.linkTarget.get), l))

      // link traversed, return the link
      case l: Linkable if l.linkTarget.isDefined => Some(l)

      // Link inside a dynamic part of the graph, typically in a trait/resource node
      case l: LinkNode => resolveDynamicLink(l)

      // no link
      case other => Some(other)

    }
  }

  def withName(resolved: DomainElement, source: DomainElement): DomainElement = {
    resolved match {
      case r: NamedDomainElement if r.name.value().notNull.isEmpty =>
        source match {
          case s: NamedDomainElement if s.name.value().notNull.nonEmpty => r.withName(s.name.value())
          case _                                                        =>
        }
      case _ =>
    }

    // let's annotate the resolved name
    source match {

      case s: NamedDomainElement if s.name.value().notNull.nonEmpty =>
        val resolvedNamesPresent          = resolved.annotations.find(classOf[ResolvedNamedEntity])
        val resolvedNamedEntityAnnotation = resolvedNamesPresent.getOrElse(ResolvedNamedEntity())
        val referenced                    = resolvedNamedEntityAnnotation.vals.getOrElse(s.name.value(), Nil)
        resolvedNamedEntityAnnotation.vals.put(s.name.value(), referenced ++ Seq(s))
        if (resolvedNamesPresent.isEmpty)
          resolved.annotations += resolvedNamedEntityAnnotation

      case s: Linkable if s.isInstanceOf[NamedDomainElement] && s.linkLabel.isDefined =>
        val resolvedNamesPresent          = resolved.annotations.find(classOf[ResolvedNamedEntity])
        val resolvedNamedEntityAnnotation = resolvedNamesPresent.getOrElse(ResolvedNamedEntity())
        val referenced                    = resolvedNamedEntityAnnotation.vals.getOrElse(s.linkLabel.get, Nil)
        resolvedNamedEntityAnnotation.vals.put(s.linkLabel.get, referenced ++ Seq(s.asInstanceOf[NamedDomainElement]))
        if (resolvedNamesPresent.isEmpty)
          resolved.annotations += resolvedNamedEntityAnnotation

      case _ => // ignore
    }

    resolved
  }

  def resolveLinked(element: DomainElement): DomainElement = {
    if (mutuallyRecursive.contains(element.id)) {
      element
    } else {
      val nested = Document()
      nested.fields.setWithoutId(DocumentModel.Encodes, element)
      val result = new ReferenceResolutionStage(profile)
        .recursiveResolveInvocation(nested, modelResolver, mutuallyRecursive ++ Seq(element.id))
      result.asInstanceOf[Document].encodes
    }
  }

}
