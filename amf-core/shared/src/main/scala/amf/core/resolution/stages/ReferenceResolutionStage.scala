package amf.core.resolution.stages
import amf.core.annotations.{DeclaredElement, ResolvedInheritance, ResolvedLinkAnnotation}
import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document, EncodesModel}
import amf.core.model.domain._
import amf.core.parser.{Annotations, ErrorHandler}
import amf.core.vocabulary.Namespace
import org.mulesoft.common.core.Strings

import scala.collection.mutable

class ReferenceResolutionStage(keepEditingInfo: Boolean, links: mutable.Map[String, DomainElement] = mutable.Map())(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage {

  var modelResolver: Option[ModelReferenceResolver] = None

  override def resolve[T <: BaseUnit](model: T): T = {
    this.modelResolver = Some(new ModelReferenceResolver(model))
    model.transform(selector, transformation).asInstanceOf[T]
  }

  private def selector(element: DomainElement): Boolean = {
    element match {
      case l: Linkable if l.isLink => true
      case _: LinkNode             => true
      case _                       => false
    }
  }

  private def transformation(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case l: Linkable if l.linkTarget.isDefined =>
        if (links.contains(l.linkTarget.get.id)) Some(links(l.linkTarget.get.id))
        else {
          val target = l.effectiveLinkTarget() match {
            case t: DomainElement if links.contains(t.id) => links(t.id)
            case t: DomainElement with Linkable if element.annotations.contains(classOf[DeclaredElement]) =>
              val copied = t.copyElement().withId(element.id)
              element match {
                case n: NamedDomainElement if n.name.option().isDefined =>
                  copied.asInstanceOf[NamedDomainElement].withName(n.name.value(), n.name.annotations())
                case _ => // ignore
              }
              copied
            case d: DomainElement => d
          }
          val resolved = innerLinkNodeResolution(target)
          resolved match {
            case linkable: Linkable if l.supportsRecursion.option().getOrElse(false) =>
              linkable.withSupportsRecursion(true)
            case _ => // ignore
          }
          val resolved2 = customDomainElementTransformation(withName(resolved, l), l)

          resolved2.annotations += ResolvedInheritance()
          if (keepEditingInfo) resolved2.annotations += ResolvedLinkAnnotation(l.id)
          traverseLinks(element, resolved2)
          Some(resolved2)
        }
      case ln: LinkNode => LinkNodeResolver.resolveDynamicLink(ln, modelResolver, keepEditingInfo)
      case _            => None
    }
  }

  // Links traversion to expand annotations and add links to 'cache'
  private def traverseLinks(element: DomainElement,
                            resolved: DomainElement,
                            visited: mutable.Set[String] = mutable.Set()): Unit = {
    if (!visited.contains(element.id)) {
      visited += element.id
      element match {
        case l: Linkable if l.linkTarget.isDefined =>
          adoptParentAnnotations(element.annotations, resolved)
          if (element.annotations.contains(classOf[DeclaredElement])) links.put(element.id, resolved)
          traverseLinks(l.linkTarget.get, resolved, visited)
        case _ => // nothing to do
      }
    }
  }

  private def adoptParentAnnotations(parentAnnotations: Annotations, child: DomainElement): Unit = {
    parentAnnotations.foreach { a =>
      // Only annotation DeclaredElement is added
      if (a.isInstanceOf[DeclaredElement] && !child.annotations.contains(a.getClass)) child.annotations += a
    }
  }

  private def innerLinkNodeResolution(target: DomainElement): DomainElement = {
    val nested = Document()
    nested.fields.setWithoutId(DocumentModel.Encodes, target)
    val result = new LinkNodeResolutionStage(keepEditingInfo).resolve(nested)
    result.asInstanceOf[Document].encodes
  }

  private def withName(resolved: DomainElement, source: DomainElement): DomainElement = {
    resolved match {
      case r: NamedDomainElement =>
        if (isExample(r)) {
          source match {
            case s: NamedDomainElement if s.name.value().notNull.nonEmpty =>
              r.withName(s.name.value(), r.name.annotations())
            case _ =>
          }
        } else if (r.name.value().notNull.isEmpty || r.name.value() == "schema" || r.name.value() == "type") {
          source match {
            case s: Linkable => innerName(s, r)
            case _           =>
          }
        }
      case _ =>
    }
    // let's annotate the resolved name
    source match {

      case s: NamedDomainElement if s.name.nonEmpty =>
        val resolvedNamesPresent          = resolved.annotations.find(classOf[ResolvedNamedEntity])
        val resolvedNamedEntityAnnotation = resolvedNamesPresent.getOrElse(ResolvedNamedEntity())
        val referenced                    = resolvedNamedEntityAnnotation.vals.getOrElse(s.name.value(), Nil)
        resolvedNamedEntityAnnotation.vals.put(s.name.value(), referenced ++ Seq(s))
        if (resolvedNamesPresent.isEmpty)
          resolved.annotations += resolvedNamedEntityAnnotation

      case s: Linkable if s.isInstanceOf[NamedDomainElement] && s.linkLabel.option().isDefined =>
        val resolvedNamesPresent          = resolved.annotations.find(classOf[ResolvedNamedEntity])
        val resolvedNamedEntityAnnotation = resolvedNamesPresent.getOrElse(ResolvedNamedEntity())
        val referenced                    = resolvedNamedEntityAnnotation.vals.getOrElse(s.linkLabel.value(), Nil)
        resolvedNamedEntityAnnotation.vals
          .put(s.linkLabel.value(), referenced ++ Seq(s.asInstanceOf[NamedDomainElement]))
        if (resolvedNamesPresent.isEmpty)
          resolved.annotations += resolvedNamedEntityAnnotation

      case _ => // ignore
    }

    resolved
  }

  private def innerName(source: DomainElement with Linkable, resolved: DomainElement with NamedDomainElement): Unit =
    source match {
      case s: NamedDomainElement =>
        s.name.option() match {
          case Some("schema" | "type") | None if source.isLink =>
            source.linkTarget match {
              case Some(target: Linkable) => innerName(target, resolved)
              case _                      =>
            }
          case Some(other) => resolved.withName(other, resolved.name.annotations())
          case _           =>
        }
      case _ =>
    }

  /** Check if it is an example. Special case where NamedExample fragments are used from an 'example' facet. */
  private def isExample(r: DomainElement) =
    r.meta.`type`.headOption.contains(Namespace.Document + "Example")

// Customisation of the resolution transformation for different domains
  protected def customDomainElementTransformation(d: DomainElement, source: Linkable): DomainElement = d

  def resolveDomainElement[T <: DomainElement](element: T): T = {
    val doc = Document().withId("http://resolutionstage.com/test#")
    if (element.id != null) {
      doc.fields.setWithoutId(DocumentModel.Encodes, element)
    } else {
      doc.withEncodes(element)
    }
    resolve(doc).encodes.asInstanceOf[T]
  }

  def resolveDomainElementSet[T <: DomainElement](elements: Seq[T]): Seq[DomainElement] = {
    val doc = Document().withId("http://resolutionstage.com/test#")

    doc.withDeclares(elements)
    resolve(doc).declares
  }
}

class LinkNodeResolutionStage(keepEditingInfo: Boolean, val visited: mutable.Set[String] = mutable.Set())(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage {

  var modelResolver: Option[ModelReferenceResolver] = None

  override def resolve[T <: BaseUnit](model: T): T = {
    this.modelResolver = Some(new ModelReferenceResolver(model))
    model.transform(selector, transformation).asInstanceOf[T]
  }

  private def selector(element: DomainElement): Boolean = {
    if (visited.contains(element.id))
      true
    else {
      visited += element.id
      element match {
        case l: Linkable if l.isLink => true
        case _: LinkNode             => true
        case _                       => false
      }
    }
  }

  private def transformation(element: DomainElement, cycle: Boolean): Option[DomainElement] = {
    element match {
      case ln: LinkNode => LinkNodeResolver.resolveDynamicLink(ln, modelResolver, keepEditingInfo)
      case _            => Some(element)
    }
  }
}

/**
  * Class that holds a resolved dynamic link node, with the original link information
  * @param source the link node that has been resolved
  * @param resolved the piece of domain linked through the resolved link node
  */
class ResolvedLinkNode(val source: LinkNode, val resolved: DomainElement)
// resolved so alias -> value
    extends LinkNode(source.value, source.value, source.fields, source.annotations) {
  linkedDomainElement = Some(resolved)

  override def cloneNode(): ResolvedLinkNode = new ResolvedLinkNode(source, resolved)
}

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
      case encodes: EncodesModel if model.location().exists(_.equals(url)) => Some(encodes.encodes)
      case _ if model.location().exists(_.equals(url))                     => None
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

object LinkNodeResolver {

  def resolveDynamicLink(l: LinkNode,
                         modelResolver: Option[ModelReferenceResolver],
                         keepEditingInfo: Boolean): Option[DomainElement] = {
    l match {
      case r: ResolvedLinkNode => Some(r)
      case _ =>
        modelResolver.get.findFragment(l.value) match {
          case Some(elem) =>
            val resolved = new ResolvedLinkNode(l, elem).withId(l.id)
            if (keepEditingInfo) resolved.annotations += ResolvedLinkAnnotation(l.id)
            resolved.annotations += ResolvedInheritance()
            if (elem.annotations.contains(classOf[DeclaredElement])) resolved.annotations += DeclaredElement()
            Some(resolved)
          case None if l.linkedDomainElement.isDefined =>
            val resolved = new ResolvedLinkNode(l, l.linkedDomainElement.get).withId(l.id)
            if (keepEditingInfo) resolved.annotations += ResolvedLinkAnnotation(l.id)
            if (l.annotations.contains(classOf[DeclaredElement])) resolved.annotations += DeclaredElement()
            resolved.annotations += ResolvedInheritance()
            Some(resolved)
          case _ =>
            Some(l)
        }
    }
  }
}
