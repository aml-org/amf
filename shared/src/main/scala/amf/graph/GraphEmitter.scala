package amf.graph

import amf.common.AMFToken._
import amf.common.Strings.strings
import amf.common._
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.metadata.Type.{Array, Bool, Iri, RegExp, Str}
import amf.metadata.document.DocumentModel
import amf.metadata.domain._
import amf.metadata.{Field, Obj, Type}
import amf.model.AmfElement
import amf.parser.{AMFASTFactory, ASTEmitter}
import amf.vocabulary.Namespace

import scala.collection.immutable.ListMap

/**
  * AMF Graph emitter
  */
object GraphEmitter {

  def emit(unit: BaseUnit, expanded: Boolean = true): AMFAST = {
    val emitter = Emitter(ASTEmitter(AMFASTFactory()), expanded)
    emitter.root(unit)
  }

  case class Emitter(e: ASTEmitter[AMFToken, AMFAST], expanded: Boolean) {

    private val ctx = context()

    def root(unit: BaseUnit): AMFAST = {

      val content = () =>
        map { () =>
          createContextNode()
          traverse(unit, unit.location)
      }

      e.root(Root)(if (expanded) { () =>
        array { () =>
          content()
        }
      } else content)
    }

    def traverse(element: AmfElement, parent: String): Unit = {
      val id = element.id(parent)
      createIdNode(element, id)

      val obj = metamodel(element)
      createTypeNode(element, obj)

      obj.fields.map(element.fields.entry).foreach {
        case Some((f, v)) =>
          entry { () =>
            raw(ctx.reduce(f.value))
            value(f.`type`, v, id)
          }
        case None => // Missing field
      }
    }

    private def value(t: Type, v: Value, parent: String) = {
      t match {
        case _: Obj       => obj(v.value.asInstanceOf[AmfElement], parent)
        case Iri          => iri(v.value.asInstanceOf[String])
        case Str | RegExp => scalar(v.value.asInstanceOf[String])
        case Bool         => scalar(v.value.asInstanceOf[Boolean].toString, BooleanToken)
        case a: Array =>
          array { () =>
            a.element match {
              case _: Obj => v.value.asInstanceOf[List[AmfElement]].foreach(e => obj(e, parent, inArray = true))
              case Str    => v.value.asInstanceOf[List[String]].foreach(scalar(_, inArray = true))
            }
          }
      }
    }

    private def obj(element: AmfElement, parent: String, inArray: Boolean = false) = {
      val obj = () =>
        map { () =>
          traverse(element, parent)
      }
      if (inArray) {
        obj()
      } else {
        array { () =>
          obj()
        }
      }
    }

    private def raw(content: String, token: AMFToken = StringToken): Unit = {
      e.value(token, if (token == StringToken) { content.quote } else content)
    }

    private def iri(content: String): Unit = {
      val e = () =>
        entry { () =>
          raw("@id")
          raw(content)
      }

      if (expanded) {
        array { () =>
          map { () =>
            e()
          }
        }
      } else e()
    }

    private def scalar(content: String, token: AMFToken = StringToken, inArray: Boolean = false): Unit = {
      if (expanded) {
        if (inArray) {
          value(content, token)
        } else {
          array { () =>
            value(content, token)
          }
        }
      } else raw(content, token)
    }

    private def value(content: String, token: AMFToken) = {
      map { () =>
        entry { () =>
          raw("@value")
          raw(content, token)
        }
      }
    }

    private def createIdNode(element: AmfElement, id: String) = entry("@id", id)

    private def createTypeNode(element: AmfElement, obj: Obj) = {
      entry { () =>
        raw("@type")
        array { () =>
          obj.`type`.foreach(t => raw(ctx.reduce(t)))
        }
      }
    }

    private def createContextNode() = {
      if (ctx != EmptyGraphContext) {
        entry { () =>
          raw("@context")
          map { () =>
            ctx.mappings {
              case (alias, ns) => entry(alias, ns.base)
            }
          }
        }
      }
    }

    private def entry(k: String, v: String): Unit = entry { () =>
      raw(k)
      raw(v)
    }

    private def entry(inner: () => Unit): Unit = node(Entry)(inner)

    private def array(inner: () => Unit): Unit = node(SequenceToken)(inner)

    private def map(inner: () => Unit): Unit = node(MapToken)(inner)

    private def node(t: AMFToken)(inner: () => Unit) = {
      e.beginNode()
      inner()
      e.endNode(t)
    }

    private def context(): GraphContext = {
      if (expanded) {
        EmptyGraphContext
      } else {
        MapGraphContext(
          ListMap(
            "raml-doc"    -> Namespace.Document,
            "raml-http"   -> Namespace.Http,
            "raml-shapes" -> Namespace.Shapes,
            "hydra"       -> Namespace.Hydra,
            "shacl"       -> Namespace.Shacl,
            "schema-org"  -> Namespace.Schema,
            "xsd"         -> Namespace.Xsd
          ))
      }
    }
  }

  private def emit(e: ASTEmitter[AMFToken, AMFAST], unit: BaseUnit, context: GraphContext): Unit = {
    e.beginNode()

  }

  /** Metadata Type references. */
  private def metamodel(instance: Any): Obj = instance match {
    case _: Document     => DocumentModel
    case _: WebApi       => WebApiModel
    case _: Organization => OrganizationModel
    case _: License      => LicenseModel
    case _: CreativeWork => CreativeWorkModel
    case _: EndPoint     => EndPointModel
    case _: Operation    => OperationModel
    case _               => throw new Exception(s"Missing metadata mapping for $instance")
  }
}
