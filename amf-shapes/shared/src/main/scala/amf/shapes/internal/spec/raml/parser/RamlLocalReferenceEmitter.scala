package amf.shapes.internal.spec.raml.parser

import amf.core.client.scala.model.domain.Linkable
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, pos, raw}
import amf.core.internal.render.emitters.PartEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.PartBuilder

class RamlLocalReferenceEntryEmitter(override val key: String, reference: Linkable)
    extends EntryPartEmitter(key, RamlLocalReferenceEmitter(reference))

case class RamlLocalReferenceEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = reference.linkLabel.option() match {
    case Some(label) => raw(b, label)
    case None        => throw new Exception("Missing link label")
  }

  override def position(): Position = pos(reference.annotations)
}
