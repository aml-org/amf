package amf.core.emitter
import amf.core.emitter.DocBuilder.SType._
import amf.core.emitter.DocBuilder.{SType, Scalar}
import amf.core.parser.SyamlParsedDocument
import amf.core.rdf.{RdfModel, RdfModelDocument}
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

abstract class DocBuilder[T] {

  /** Return the result document */
  def result: T

  /** Returns true if the documents is defined (i.e. result will return a valid document ) */
  def isDefined: Boolean

  /** Build a List document*/
  def list(f: Part => Unit): Unit

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

abstract class ParsedDocBuilder[T, D] extends DocBuilder[T] {
  private var _document: Option[D] = None
  def document: D                  = _document.get
  def document_=(doc: D): Unit     = _document = Some(doc)
  override def isDefined: Boolean  = _document.isDefined
}

class YDocumentBuilder extends ParsedDocBuilder[SyamlParsedDocument, YDocument] {

  var comment: Option[YComment]            = None
  override def result: SyamlParsedDocument = SyamlParsedDocument(document, comment)

  override def list(f: Part => Unit): Unit =
    document = YDocument.fromNode(createSeqNode(f))

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

class RdfModelBuilder extends ParsedDocBuilder[RdfModelDocument, RdfModel] {
  override def result: RdfModelDocument = RdfModelDocument(document)

  /** Build a List document*/
  override def list(f: Part => Unit): Unit = ???
}
