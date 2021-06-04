package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import org.yaml.model.YDocument.EntryBuilder

case class RamlXoneConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.xone.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "xone".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
