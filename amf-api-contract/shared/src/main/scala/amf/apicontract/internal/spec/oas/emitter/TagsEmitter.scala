package amf.apicontract.internal.spec.oas.emitter

import amf.apicontract.client.scala.model.domain.Tag
import amf.apicontract.internal.metamodel.domain.TagModel
import amf.apicontract.internal.spec.common.emitter.SpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class TagsEmitter(key: String, tags: Seq[Tag], ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = tags.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)

  override def emit(b: EntryBuilder): Unit = {
    val emitters = tags.map(t => TagEmitter(t, ordering))
    b.entry(
      key,
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }
}

case class TagEmitter(tag: Tag, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def position(): Position = pos(tag.annotations)

  override def emit(p: PartBuilder): Unit = {
    p.obj { b =>
      val fs     = tag.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(TagModel.Name).map(f => result += ValueEmitter("name", f))
      fs.entry(TagModel.Description).map(f => result += ValueEmitter("description", f))
      fs.entry(TagModel.Documentation)
        .map(_ =>
          result +=
            OasEntryCreativeWorkEmitter("externalDocs", tag.documentation, ordering))

      result ++= AnnotationsEmitter(tag, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }
}
