package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.PartEmitter
import amf.core.model.domain.Linkable
import amf.core.parser.Position
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

case class RamlExternalRefEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit =
    b += YNode.include(reference.linkLabel.option().getOrElse(reference.location().get))
  override def position(): Position = pos(reference.annotations)
}
