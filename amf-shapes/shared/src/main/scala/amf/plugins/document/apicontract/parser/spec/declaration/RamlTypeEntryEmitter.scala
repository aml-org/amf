package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml.Raml10TypeEmitter
import org.yaml.model.YDocument.EntryBuilder

case class RamlTypeEntryEmitter(key: String, shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(key, _.obj { b =>
      val emitters = Raml10TypeEmitter(shape, ordering, references = references).entries()
      traverse(ordering.sorted(emitters), b)
    })
  }

  override def position(): Position = pos(shape.annotations)
}
