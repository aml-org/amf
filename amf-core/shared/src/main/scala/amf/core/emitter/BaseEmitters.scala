package amf.core.emitter

import amf.core.annotations.{DomainExtensionAnnotation, LexicalInformation, SingleValueArray}
import amf.core.metamodel.{Field, Type}
import amf.core.model.domain.AmfScalar
import amf.core.parser.Position._
import amf.core.parser.{Annotations, FieldEntry, Position, Value}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model._

import scala.collection.mutable

package object BaseEmitters {

  protected[amf] def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected[amf] def traverse(emitters: Seq[EntryEmitter], b: EntryBuilder): Unit = {
    emitters.foreach(e => {
      e.emit(b)
    })
  }

  protected[amf] def traverse(emitters: Seq[PartEmitter], b: PartBuilder): Unit = {
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

  case class LinkScalaEmitter(alias: String, annotations: Annotations) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      sourceOr(annotations, {
        b += YNode(new YScalar.Builder(alias, YType.Include.tag).scalar, YType.Include) // YNode(YScalar(alias), YType.Include)
      })
    }

    override def position(): Position = pos(annotations)
  }

  trait BaseValueEmitter extends EntryEmitter {

    val key: String
    val f: FieldEntry

    val tag: YType = {
      f.field.`type` match {
        case Type.Int  => YType.Int
        case Type.Bool => YType.Bool
        case _         => YType.Str
      }
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry) extends BaseValueEmitter {

    override def emit(b: EntryBuilder): Unit = sourceOr(f.value, simpleScalar(b))

    private def simpleScalar(b: EntryBuilder): Unit = {
      b.entry(
        key,
        YNode(YScalar(f.scalar.value), tag)
      )
    }
  }

  object RawValueEmitter {
    def apply(key: String, f: Field, value: Any, annotations: Annotations = Annotations()) = ValueEmitter(
      key,
      FieldEntry(f, Value(AmfScalar(value, Annotations()), annotations))
    )
  }

  protected[amf] def sourceOr(value: Value, inner: => Unit): Unit = sourceOr(value.annotations, inner)

  protected[amf] def sourceOr(annotations: Annotations, inner: => Unit): Unit = {
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

  case class EmptyMapEmitter(position: Position = Position.ZERO) extends PartEmitter {

    override def emit(b: PartBuilder): Unit = b += YMap.empty
  }

  protected[amf] def link(b: PartBuilder, id: String): Unit = b.obj(_.entry("@id", id.trim))

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, force: Boolean = false)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val single = f.value.annotations.contains(classOf[SingleValueArray]) ||
        f.value.value.annotations.contains(classOf[SingleValueArray])

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
