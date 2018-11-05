package amf.core.emitter
import amf.core.emitter.DocBuilder.SType._
import amf.core.emitter.DocBuilder.{SType, Scalar}
import amf.core.parser.SyamlParsedDocument
import org.mulesoft.common.core._
import org.mulesoft.common.io.Output
import org.mulesoft.common.io.Output._
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

abstract class DocBuilder[T] {

  /** Return the result document */
  def result: T

  /** Returns true if the documents is defined (i.e. result will return a valid document ) */
  def isDefined: Boolean = true

  /** Build a List document*/
  def list(f: Part => Unit): Unit

  /** Build an Object document*/
  def obj(f: Entry => Unit): Unit

  abstract class Part {

    def +=(int: Int): Unit    = this += Scalar(Int, int)
    def +=(str: String): Unit = this += Scalar(str)
    def +=(dbl: Double): Unit = this += Scalar(Float, dbl)
    def +=(b: Boolean): Unit  = this += Scalar(Bool, b)

    def +=(scalar: Scalar): Unit

    /** Add a List to the builder */
    def list(f: Part => Unit): Unit

    /** Add an object (aka map) to the builder */
    def obj(f: Entry => Unit): Unit
  }
  abstract class Entry {
    def entry(key: String, f: Part => Unit): Unit
    def entry(key: String, value: Scalar): Unit
    def entry(key: String, value: String): Unit           = entry(key, Scalar(value))
    def entry(key: String, t: SType, value: String): Unit = entry(key, Scalar(t, value))
  }
}

object DocBuilder {
  sealed trait SType
  object SType {
    case object Str   extends SType
    case object Float extends SType
    case object Int   extends SType
    case object Bool  extends SType
  }

  case class Scalar(t: SType, value: Any)

  object Scalar {

    def apply(value: String): Scalar = new Scalar(Str, value)

    def apply(t: SType, value: String): Scalar = t match {
      case Str   => Scalar(value)
      case Int   => tryMake(new Scalar(Int, value.toLong), value)
      case Float => tryMake(new Scalar(Float, value.toDouble), value)
      case Bool  => tryMake(new Scalar(Bool, value.toBoolean), value)
    }

    def tryMake[U](make: => Scalar, str: String): Scalar =
      try make
      catch {
        case NonFatal(_) => Scalar(str)
      }
  }
}

class YDocumentBuilder extends DocBuilder[SyamlParsedDocument] {

  private var _document: Option[YDocument] = None
  def document: YDocument                  = _document.get
  def document_=(doc: YDocument): Unit     = _document = Some(doc)
  override def isDefined: Boolean          = _document.isDefined

  var comment: Option[YComment]            = None
  override def result: SyamlParsedDocument = SyamlParsedDocument(document, comment)

  override def list(f: Part => Unit): Unit =
    document = YDocument.fromNode(createSeqNode(f))

  override def obj(f: Entry => Unit): Unit =
    document = YDocument.fromNode(createMapNode(f))

  private def createPartBuilder(f: Part => Unit): ArrayBuffer[YNode] = {
    val builder = new ArrayBuffer[YNode]
    val partBuilder = new Part {
      override def +=(scalar: Scalar): Unit    = builder += mkNode(scalar)
      override def list(f: Part => Unit): Unit = builder += createSeqNode(f)
      override def obj(f: Entry => Unit): Unit = builder += createMapNode(f)
    }
    f(partBuilder)
    builder
  }

  private def mkNode(scalar: Scalar): YNode = scalar.t match {
    case Str   => scalar.value.toString
    case Bool  => scalar.value.asInstanceOf[Boolean]
    case Float => scalar.value.asInstanceOf[Double]
    case Int   => scalar.value.asInstanceOf[Long]
  }

  private def createMapNode(f: Entry => Unit) = {
    val builder                                  = new ArrayBuffer[YPart]
    def addEntry(key: String, node: YNode): Unit = builder += YMapEntry(Array(YNode(key), node))

    val b = new Entry {
      override def entry(key: String, value: Scalar): Unit   = addEntry(key, mkNode(value))
      override def entry(key: String, f: Part => Unit): Unit = addEntry(key, createPartBuilder(f).result()(0))

    }
    f(b)
    YNode(YMap(builder.result, ""), YType.Map)
  }
  private def createSeqNode(f: Part => Unit) = YNode(YSequence(createPartBuilder(f).result, ""), YType.Seq)

}

class OutputBuilder[W: Output](val writer: W, val prettyPrint: Boolean = false) extends DocBuilder[W] {

  override def result: W = writer

  override def list(f: Part => Unit): Unit = emitSeq(f)
  override def obj(f: Entry => Unit): Unit = emitMap(f)

  private def emitParts(f: Part => Unit): Unit = f(new MyPart)

  class MyPart extends Part {
    override def +=(scalar: Scalar): Unit    = { before(); emitNode(scalar) }
    override def list(f: Part => Unit): Unit = { before(); emitSeq(f) }
    override def obj(f: Entry => Unit): Unit = { before(); emitMap(f) }
  }
  class MyEntry extends Entry {
    override def entry(key: String, value: Scalar): Unit = {
      emitKey(key)
      emitNode(value)
    }
    private def emitKey(key: String): Unit = {
      before()
      writer.append(key)
      writer.append(": ")
    }
    override def entry(key: String, f: Part => Unit): Unit = {
      emitKey(key)
      emitParts(f)
    }

  }

  private def emitNode(scalar: Scalar): Unit = (scalar.t, scalar.value) match {
    case (Str, s: String)   => writer.append('"' + s.encode + '"')
    case (Bool, b: Boolean) => writer.append(b.toString)
    case (Int, l: Long)     => writer.append(l.toString)
    case (Float, v: Double) =>
      var s = v.toString
      if (s.indexOf('.') == -1) s += ".0" // Bug in scala-js toString
      writer.append(s)
    case _ =>
  }

  private def emitMap(f: Entry => Unit): Unit = {
    writer.append('{')
    indent()
    f(new MyEntry)
    dedent()
    renderIndent()
    writer.append('}')
  }
  private def emitSeq(f: Part => Unit): Unit = {
    writer.append('[')
    indent()
    emitParts(f)
    dedent()
    renderIndent()
    writer.append(']')
  }

  private var indentation = ""
  private var start       = true

  private def indent(): Unit = indentation += "  "
  private def dedent(): Unit = indentation = indentation.substring(2)

  private def renderIndent(): Unit =
    if (prettyPrint && indentation.nonEmpty) writer.append(indentation)

  private def before(): Unit = {
    if (start) start = false else writer.append(',')
    writer.append('\n')
    renderIndent()
  }

}
