package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
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
