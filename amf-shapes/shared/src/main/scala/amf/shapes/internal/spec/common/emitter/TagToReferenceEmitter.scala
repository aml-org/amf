package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, pos, raw}
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.internal.spec.common.JSONSchemaVersion
import amf.shapes.internal.spec.oas.OasShapeDefinitions.appendSchemasPrefix
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

/** */
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
      if (isDeclaredElement(lastElementInLinkChain)) getRefUrlFor(lastElementInLinkChain)(shapeSpec) else referenceLabel
    link match {
      case l: Linkable => shapeSpec.ref(b, urlToEmit, l)
      case _           => shapeSpec.ref(b, urlToEmit)
    }
  }

  protected def getRefUrlFor(element: DomainElement, default: String = referenceLabel)(implicit
      spec: ShapeEmitterContext
  ): String = {
    val jsonSchemaVersion = Some(spec.schemaVersion).collect { case version: JSONSchemaVersion => version }
    element match {
      case _: Shape => appendSchemasPrefix(referenceLabel, Some(shapeSpec.spec), jsonSchemaVersion)
      case _        => default
    }
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

  def emitLinkOr(l: DomainElement with Linkable, b: PartBuilder, refs: Seq[BaseUnit] = Nil)(
      fallback: => Unit
  )(implicit spec: ShapeEmitterContext): Unit = {
    if (l.isLink)
      spec.tagToReferenceEmitter(l, refs).emit(b)
    else
      fallback
  }
}

trait RefEmitter {
  def ref(url: String, b: PartBuilder, l: Linkable): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}

object RamlRefEmitter extends RefEmitter {
  override def ref(url: String, b: PartBuilder, l: Linkable): Unit = b += YNode.include(url)
}

object OasRefEmitter extends RefEmitter {
  override def ref(url: String, b: PartBuilder, l: Linkable): Unit = {
    val summary     = if (l.refSummary.nonEmpty) Some(l.refSummary.value()) else None
    val description = if (l.refDescription.nonEmpty) Some(l.refDescription.value()) else None
    b.obj(entryBuilder => {
      entryBuilder.entry("$ref", raw(_, url))
      summary.foreach(s => entryBuilder.entry("summary", raw(_, s)))
      description.foreach(d => entryBuilder.entry("description", raw(_, d)))
    })
  }
}
