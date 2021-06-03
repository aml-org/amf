package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{FieldEntry, Position}
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  ShapeEmitterContext
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.OasPropertyShapeEmitter
import org.yaml.model.YDocument.EntryBuilder

case class OasCustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeShapeEmitterContext)
    extends CustomFacetsEmitter(f, ordering, references) {

  override val key: String = "facets".asOasExtension

  override def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter =
    (p: PropertyShape, o: SpecOrdering, s: Seq[BaseUnit]) => OasPropertyShapeEmitter.apply(p, o, s)
}

abstract class CustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])
    extends EntryEmitter {

  val key: String
  def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter

  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      key,
      _.obj { b =>
        val result = f.array.values.map { v =>
          shapeEmitter(v.asInstanceOf[PropertyShape], ordering, references)
        }
        traverse(ordering.sorted(result), b)
      }
    )

  }

  override def position(): Position = pos(f.value.annotations)
}
