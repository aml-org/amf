package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml.Raml10TypeEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  RamlShapeEmitterContext,
  ShapeEmitterContext,
  raml
}
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
