package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{SpecOrdering, EntryEmitter}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Position, FieldEntry}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext

case class RamlCustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends CustomFacetsEmitter(f, ordering, references) {

  override val key: String = "facets"

  override def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter =
    RamlPropertyShapeEmitter.apply
}

case class OasCustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends CustomFacetsEmitter(f, ordering, references) {

  override val key: String = "facets".asOasExtension

  override def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter =
    (p: PropertyShape, o: SpecOrdering, s: Seq[BaseUnit]) => OasPropertyShapeEmitter.apply(p, o, s)
}

abstract class CustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
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
