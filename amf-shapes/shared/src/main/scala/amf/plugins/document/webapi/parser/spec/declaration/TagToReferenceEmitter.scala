package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.{DeclaredElement, ExternalFragmentRef}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.PartEmitter
import amf.core.model.document.{BaseUnit, Fragment}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.OasShapeDefinitions.{appendOas3ComponentsPrefix, appendSchemasPrefix}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasSpecEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

/**
  *
  */
trait TagToReferenceEmitter extends PartEmitter {
  val link: DomainElement

  val label: Option[String] = link match {
    case l: Linkable => l.linkLabel.option()
    case _           => None
  }

  val referenceLabel: String = label.getOrElse(link.id)
}

trait ShapeReferenceEmitter extends TagToReferenceEmitter {

  protected val shapeSpec: ShapeEmitterContext

  def emit(b: PartBuilder): Unit = {
    val lastElementInLinkChain = follow()
    val urlToEmit =
      if (isDeclaredElement(lastElementInLinkChain)) getRefUrlFor(lastElementInLinkChain) else referenceLabel
    shapeSpec.ref(b, urlToEmit)
  }

  protected def getRefUrlFor(element: DomainElement, default: String = referenceLabel) = element match {
    case _: Shape => appendSchemasPrefix(referenceLabel, Some(shapeSpec.vendor))
    case _        => default
  }

  private def isDeclaredElement(element: DomainElement) = element.annotations.contains(classOf[DeclaredElement])

  /** Follow links until first declaration or last element in chain */
  private def follow(): DomainElement = follow(link)

  @scala.annotation.tailrec
  private def follow(element: DomainElement, seenLinks: Seq[String] = Seq()): DomainElement = {
    element match {
      case s: Linkable if s.isLink =>
        s.linkTarget match {
          case Some(t: Linkable) if t.isLink & !t.annotations.contains(classOf[DeclaredElement]) =>
            // If find link which is not a declaration (declarations can be links as well) follow link
            follow(t.linkTarget.get, seenLinks :+ element.id)
          case Some(t) => t
          case None    => s // This is unreachable
        }
      case other => other
    }
  }
}

case class OasShapeReferenceEmitter(link: DomainElement)(implicit val shapeSpec: ShapeEmitterContext)
    extends ShapeReferenceEmitter {

  override def position(): Position = pos(link.annotations)
}

object ReferenceEmitterHelper {

  def emitLinkOr(l: DomainElement with Linkable, b: PartBuilder, refs: Seq[BaseUnit] = Nil)(fallback: => Unit)(
      implicit spec: ShapeEmitterContext): Unit = {
    if (l.isLink)
      spec.tagToReferenceEmitter(l, refs).emit(b)
    else
      fallback
  }
}

trait RefEmitter {
  def ref(url: String, b: PartBuilder): Unit
}

case class RamlTagToReferenceEmitter(link: DomainElement, references: Seq[BaseUnit])(
    implicit val spec: ShapeEmitterContext)
    extends PartEmitter
    with TagToReferenceEmitter {

  override def emit(b: PartBuilder): Unit = {
    if (containsRefAnnotation)
      link.annotations.find(classOf[ExternalFragmentRef]).foreach { a =>
        spec.ref(b, a.fragment) // emits with !include
      } else if (linkReferencesFragment)
      spec.ref(b, referenceLabel) // emits with !include
    else
      raw(b, referenceLabel)
  }

  private def containsRefAnnotation = link.annotations.contains(classOf[ExternalFragmentRef])

  private def linkReferencesFragment: Boolean = {
    link match {
      case l: Linkable =>
        l.linkTarget.exists { target =>
          references.exists {
            case f: Fragment => f.encodes == target
            case _           => false
          }
        }
      case _ => false
    }
  }

  override def position(): Position = pos(link.annotations)
}

class RamlLocalReferenceEntryEmitter(override val key: String, reference: Linkable)
    extends EntryPartEmitter(key, RamlLocalReferenceEmitter(reference))

case class RamlLocalReferenceEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = reference.linkLabel.option() match {
    case Some(label) => raw(b, label)
    case None        => throw new Exception("Missing link label")
  }

  override def position(): Position = pos(reference.annotations)
}

case class RamlIncludeReferenceEmitter(reference: Linkable, location: String) extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    raw(b, s"!include ${location}", YType.Include)

  override def position(): Position = pos(reference.annotations)
}
