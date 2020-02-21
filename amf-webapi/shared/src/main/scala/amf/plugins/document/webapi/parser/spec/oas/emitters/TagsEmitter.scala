package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, OasEntryCreativeWorkEmitter}
import amf.plugins.domain.webapi.metamodel.TagModel
import amf.plugins.domain.webapi.models.Tag
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

  private case class TagEmitter(tag: Tag, ordering: SpecOrdering) extends PartEmitter {

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
}
