package amf.spec.raml

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFToken}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.{LexicalInformation, SourceAST}
import amf.domain._
import amf.metadata.domain.{EndPointModel, LicenseModel, WebApiModel}
import amf.model.{AmfArray, AmfObject, AmfScalar}
import amf.parser.Position.ZERO
import amf.parser.{AMFASTFactory, ASTEmitter, Position}

import scala.collection.mutable

/**
  * Created by pedro.colunga on 8/17/17.
  */
case class RamlSpecEmitter(unit: BaseUnit) {

  val emitter: ASTEmitter[AMFToken, AMFAST] = ASTEmitter(AMFASTFactory())

  private def retrieveWebApi() = unit match {
    case document: Document => document.encodes
  }

  def emitWebApi(): AMFAST = {
    val api = WebApiEmitter(retrieveWebApi(), Lexical)

    emitter.root(Root) { () =>
      raw("%RAML 1.0", Comment)
      map { () =>
        traverse(api.emitters)
      }
    }
  }

  private def traverse(emitters: mutable.SortedSet[Emitter]): Unit = {
    emitters.foreach(e => {
      e.emit()
    })
  }

  private def entry(inner: () => Unit): Unit = node(Entry)(inner)

  private def array(inner: () => Unit): Unit = node(SequenceToken)(inner)

  private def map(inner: () => Unit): Unit = node(MapToken)(inner)

  private def node(t: AMFToken)(inner: () => Unit) = {
    emitter.beginNode()
    inner()
    emitter.endNode(t)
  }

  private def raw(content: String, token: AMFToken = StringToken): Unit = {
    //    emitter.value(token, if (token == StringToken) { content.quote } else content)
    emitter.value(token, content)
  }

  case class WebApiEmitter(api: WebApi, ordering: Ordering[Emitter]) {

    val emitters: mutable.SortedSet[Emitter] = {
      val fs     = api.fields
      val result = mutable.SortedSet()(ordering)

      fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

      fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(WebApiModel.Schemes).map(f => result += ArrayEmitter("protocols", f, ordering))

      fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("(license)", f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result ++= endpoints(f, ordering))

      result
    }

    private def endpoints(f: FieldEntry, ordering: Ordering[Emitter]): Seq[Emitter] = {
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .map(e => EndPointEmitter(e.asInstanceOf[EndPoint], ordering))
    }
  }

  /** [[AmfObject]] emitter */
  case class AmfObjectEmitter(obj: AmfObject, ordering: Ordering[FieldEntry]) {
    val fields: Set[FieldEntry] = {
      val result = mutable.SortedSet()(ordering)
      result ++= obj.fields.fields()
      result.toSet
    }
  }

  trait Emitter {
    def emit(): Unit
    def position(): Position
  }

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      val result = mutable.SortedSet()(ordering)

      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val result = mutable.SortedSet()(ordering)

          f.value.value
            .asInstanceOf[AmfArray]
            .values
            .foreach(v => {
              result += ScalarEmitter(v.asInstanceOf[AmfScalar])
            })

          array { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class EndPointEmitter(endpoint: EndPoint, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        endpoint.annotations,
        entry { () =>
          val fs = endpoint.fields

          ScalarEmitter(fs.entry(EndPointModel.Path).get.value.value.asInstanceOf[AmfScalar]).emit()

          val result = mutable.SortedSet()(ordering)

          fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))

          fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(endpoint.annotations)
  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.SortedSet()(ordering)

          fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ScalarEmitter(v: AmfScalar) extends Emitter {
    override def emit(): Unit = sourceOr(v.annotations, raw(v.value.toString))

    override def position(): Position = pos(v.annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.value.value.asInstanceOf[AmfScalar].value.toString)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  private def pos(annotations: Annotations): Position = {
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
  }

  private def sourceOr(value: Value, inner: => Unit): Unit = sourceOr(value.annotations, inner)

  private def sourceOr(annotations: Annotations, inner: => Unit): Unit = {
    annotations
      .find(classOf[SourceAST])
      .fold(inner)(a => emitter.addChild(a.ast))
  }

  object Default extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = 1
  }

  object Lexical extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = x.position().compareTo(y.position())
  }

}
