package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{OasEntryCreativeWorkEmitter, RamlCreativeWorkEmitter}
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  ApiShapeEmitterContextAdapter,
  ShapeEmitterContext
}

case class UserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)
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
  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "userDocumentation".asOasExtension,
      _.list(traverse(ordering.sorted(documents.map(RamlCreativeWorkEmitter(_, ordering, withExtension = false))), _))
    )
  }

  override def position(): Position = documents.headOption.map(_.annotations).map(pos).getOrElse(Position.ZERO)
}
