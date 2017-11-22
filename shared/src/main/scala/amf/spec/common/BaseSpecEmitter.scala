package amf.spec.common

import amf.document.BaseUnit
import amf.domain.Annotation.{LexicalInformation, SingleValueArray}
import amf.domain._
import amf.domain.extensions.{ArrayNode => DataArrayNode, ObjectNode => DataObjectNode, ScalarNode => DataScalarNode}
import amf.metadata.{Field, Type}
import amf.model.AmfScalar
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.remote.Vendor
import amf.spec.declaration.TagToReferenceEmitter
import amf.spec.{EntryEmitter, PartEmitter, SpecOrdering}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YNode, YScalar, YType}

import scala.collection.mutable

trait SpecEmitterContext {
  def tagToReference(l: DomainElement, linkLabel: Option[String], references: Seq[BaseUnit]): PartEmitter =
    TagToReferenceEmitter(l, linkLabel, vendor, references)

  def ref(b: PartBuilder, url: String): Unit

  def localReference(reference: Linkable): PartEmitter
  val vendor: Vendor
}

trait BaseSpecEmitter {
  implicit val spec: SpecEmitterContext
}

package object BaseEmitters {

  protected[spec] def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected[spec] def traverse(emitters: Seq[EntryEmitter], b: EntryBuilder): Unit = {
    emitters.foreach(e => {
      e.emit(b)
    })
  }

  protected[spec] def traverse(emitters: Seq[PartEmitter], b: PartBuilder): Unit = {
    emitters.foreach(e => {
      e.emit(b)
    })
  }

  case class RawEmitter(content: String, tag: YType = YType.Str, annotations: Annotations = Annotations())
      extends PartEmitter {
    override def emit(b: PartBuilder): Unit = sourceOr(annotations, raw(b, content, tag))

    override def position(): Position = pos(annotations)
  }

  def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
    b += YNode(YScalar(content), tag)

  case class ScalarEmitter(v: AmfScalar, tag: YType = YType.Str) extends PartEmitter {
    override def emit(b: PartBuilder): Unit =
      sourceOr(v.annotations, {
        b += YNode(YScalar(v.value), tag)
      })

    override def position(): Position = pos(v.annotations)
  }

  case class TextScalarEmitter(value: String, annotations: Annotations, tag: YType = YType.Str) extends PartEmitter {
    override def emit(b: PartBuilder): Unit =
      sourceOr(annotations, {
        b += YNode(new YScalar.Builder(value, tag.tag).scalar, tag)
      })

    override def position(): Position = pos(annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry) extends EntryEmitter {

    private val tag: YType = {
      f.field.`type` match {
        case Type.Int  => YType.Int
        case Type.Bool => YType.Bool
        case _         => YType.Str
      }
    }

    override def emit(b: EntryBuilder): Unit = {
      sourceOr(f.value,
               b.entry(
                 key,
                 YNode(YScalar(f.scalar.value), tag)
               ))
    }

    override def position(): Position = pos(f.value.annotations)
  }

  object RawValueEmitter {
    def apply(key: String, f: Field, value: Any, annotations: Annotations = Annotations()) = ValueEmitter(
      key,
      FieldEntry(f, Value(AmfScalar(value, Annotations()), annotations))
    )
  }

  protected[spec] def sourceOr(value: Value, inner: => Unit): Unit = sourceOr(value.annotations, inner)

  protected[spec] def sourceOr(annotations: Annotations, inner: => Unit): Unit = {
    //    annotations
    //      .find(classOf[SourceAST])
    //      .fold(inner)(a => emitter.addChild(a.ast))
    inner
  }

  case class MapEntryEmitter(key: String, value: String, tag: YType = YType.Str, position: Position = Position.ZERO)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        raw(_, value, tag)
      )
    }
  }

  case class EntryPartEmitter(key: String,
                              value: PartEmitter,
                              tag: YType = YType.Str,
                              position: Position = Position.ZERO)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      b.entry(key, value.emit _)
    }
  }

  object MapEntryEmitter {
    def apply(tuple: (String, String)): MapEntryEmitter =
      tuple match {
        case (key, value) => MapEntryEmitter(key, value)
      }
  }

  protected[amf] def link(b: PartBuilder, id: String): Unit = b.obj(_.entry("@id", id.trim))

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, force: Boolean = false)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val single = f.value.annotations.contains(classOf[SingleValueArray])

      sourceOr(
        f.value,
        if (single && !force) emitSingle(b) else emitValues(b)
      )
    }

    private def emitSingle(b: EntryBuilder): Unit = {
      val value = f.array.scalars.headOption.map(_.toString).getOrElse("")
      b.entry(key, value)
    }

    private def emitValues(b: EntryBuilder): Unit = {
      b.entry(
        key,
        b => {
          val result = mutable.ListBuffer[PartEmitter]()

          f.array.scalars
            .foreach(v => {
              result += ScalarEmitter(v)
            })

          b.list(b => {
            traverse(ordering.sorted(result), b)
          })
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

}
