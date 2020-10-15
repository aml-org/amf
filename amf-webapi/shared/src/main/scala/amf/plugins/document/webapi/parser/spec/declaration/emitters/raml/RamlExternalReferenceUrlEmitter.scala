package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.PartEmitter
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position
import org.yaml.model.{YDocument, YNode}
import amf.core.emitter.BaseEmitters.pos
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import org.yaml.model.YDocument.PartBuilder

// emit !include ref for elements that have been referenced and where inlined during parsing
case class RamlExternalReferenceUrlEmitter(element: DomainElement)(fallback: => Unit = Unit) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit =
    element.annotations.find(classOf[ExternalReferenceUrl]) match {
      case Some(annot) => b += YNode.include(annot.url)
      case None        => fallback
    }

  override def position(): Position = pos(element.annotations)
}

object ExternalReferenceUrlEmitter {

  def handleInlinedRefOr(b: PartBuilder, element: DomainElement)(fallback: => Unit): Unit = {
    RamlExternalReferenceUrlEmitter(element)(fallback).emit(b)
  }
}
