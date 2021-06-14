package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.OasShapeDefinitions
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val inherits = f.array.values.map(_.asInstanceOf[Shape])
    b.entry(
      "x-amf-merge",
      _.list(b =>
        inherits.foreach { s =>
          if (s.annotations.contains(classOf[DeclaredElement]))
            spec.ref(b, OasShapeDefinitions.appendSchemasPrefix(s.name.value(), Some(spec.vendor)))
          else if (s.linkTarget.isDefined)
            spec.ref(b, OasShapeDefinitions.appendSchemasPrefix(s.name.value(), Some(spec.vendor)))
          else OasTypePartEmitter(s, ordering, references = references).emit(b)
      })
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
