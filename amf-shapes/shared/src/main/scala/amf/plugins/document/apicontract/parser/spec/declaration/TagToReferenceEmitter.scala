package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.annotations.{DeclaredElement, ExternalFragmentRef}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.PartEmitter
import amf.core.model.document.{BaseUnit, Fragment}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.OasShapeDefinitions.{
  appendOas3ComponentsPrefix,
  appendSchemasPrefix
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.oas.emitters.OasSpecEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YNode, YType}

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

object RamlRefEmitter extends RefEmitter {
  override def ref(url: String, b: PartBuilder): Unit = b += YNode.include(url)
}

object OasRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
