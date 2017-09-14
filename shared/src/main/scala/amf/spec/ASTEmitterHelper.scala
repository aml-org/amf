package amf.spec

import amf.common.{AMFAST, AMFToken}
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.domain.Annotation.LexicalInformation
import amf.domain.{Annotations, FieldEntry, Value}
import amf.model.AmfScalar
import amf.parser.Position.ZERO
import amf.parser.{AMFASTFactory, ASTEmitter, Position}

trait ASTEmitterHelper {

  val emitter: ASTEmitter[AMFToken, AMFAST] = ASTEmitter(AMFASTFactory())

  protected def traverse(emitters: Seq[Emitter]): Unit = {
    emitters.foreach(e => {
      e.emit()
    })
  }

  protected def node(t: AMFToken)(inner: () => Unit) = {
    emitter.beginNode()
    inner()
    emitter.endNode(t)
  }
  protected def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def sourceOr(value: Value, inner: => Unit): Unit = sourceOr(value.annotations, inner)

  protected def sourceOr(annotations: Annotations, inner: => Unit): Unit = {
    //    annotations
    //      .find(classOf[SourceAST])
    //      .fold(inner)(a => emitter.addChild(a.ast))
    inner
  }
  protected def entry(inner: () => Unit): Unit = node(Entry)(inner)

  protected def array(inner: () => Unit): Unit = node(SequenceToken)(inner)

  protected def map(inner: () => Unit): Unit = node(MapToken)(inner)

  protected def raw(content: String, token: AMFToken = StringToken): Unit = {
    emitter.value(token, content)
  }

  case class ScalarEmitter(v: AmfScalar) extends Emitter {
    override def emit(): Unit = sourceOr(v.annotations, raw(v.toString))

    override def position(): Position = pos(v.annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.scalar.toString)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  /** Emit a single value from an array as an entry. */
  case class ArrayValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.array.values.headOption.map(_.asInstanceOf[AmfScalar].toString).getOrElse(""))
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

}