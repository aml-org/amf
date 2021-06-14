package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.client.common.position.Position
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.SpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.apicontract.parser.spec.declaration.{OasEntryCreativeWorkEmitter, RamlCreativeWorkEmitter}
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.YDocument.EntryBuilder

case class UserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  def emitters(): Seq[EntryEmitter] = {

    val documents: List[CreativeWork] = f.array.values.collect({ case c: CreativeWork => c }).toList

    documents match {
      case head :: Nil => Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering))
      case head :: tail =>
        Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering), CreativeWorkEmitters(tail, ordering))
      case _ => Nil
    }

  }
}

case class CreativeWorkEmitters(documents: Seq[CreativeWork], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "userDocumentation".asOasExtension,
      _.list(traverse(ordering.sorted(documents.map(RamlCreativeWorkEmitter(_, ordering, withExtension = false))), _))
    )
  }

  override def position(): Position = documents.headOption.map(_.annotations).map(pos).getOrElse(Position.ZERO)
}
