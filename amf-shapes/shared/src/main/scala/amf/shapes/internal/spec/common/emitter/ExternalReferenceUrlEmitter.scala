package amf.shapes.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.client.scala.annotations.ExternalReferenceUrl
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YDocument, YNode}

// emit ref for elements that have been referenced and where inlined during parsing
abstract class ExternalReferenceUrlEmitter(element: DomainElement, fallback: => Unit = Unit) extends PartEmitter {

  def emitRef(b: PartBuilder, url: String): Unit

  override def emit(b: YDocument.PartBuilder): Unit =
    element.annotations.find(classOf[ExternalReferenceUrl]) match {
      case Some(annot) => emitRef(b, annot.url)
      case None        => fallback
    }

  override def position(): Position = pos(element.annotations)
}

case class RamlExternalReferenceUrlEmitter(element: DomainElement)(fallback: => Unit = Unit)
    extends ExternalReferenceUrlEmitter(element, fallback) {
  override def emitRef(b: PartBuilder, url: String): Unit = b += YNode.include(url)
}

case class OasExternalReferenceUrlEmitter(element: DomainElement)(fallback: => Unit = Unit)(
    implicit val spec: ShapeEmitterContext)
    extends ExternalReferenceUrlEmitter(element, fallback) {
  override def emitRef(b: PartBuilder, url: String): Unit = spec.ref(b, url)
}

object ExternalReferenceUrlEmitter {

  def handleInlinedRefOr(b: PartBuilder, element: DomainElement)(fallback: => Unit)(
      implicit spec: ShapeEmitterContext): Unit =
    spec match {
      case _ if spec.isOasLike => OasExternalReferenceUrlEmitter(element)(fallback).emit(b)
      case _ if spec.isRaml    => RamlExternalReferenceUrlEmitter(element)(fallback).emit(b)
      case _                   =>
    }

}
