package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.Linkable
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.emitters.PartEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

case class RamlExternalRefEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit =
    b += YNode.include(reference.linkLabel.option().getOrElse(reference.location().get))
  override def position(): Position = pos(reference.annotations)
}
