package amf.graph

import amf.common.AMFToken._
import amf.common.Strings.strings
import amf.common._
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.metadata.Type.{Array, RegExp, Str}
import amf.metadata.document.DocumentModel
import amf.metadata.domain._
import amf.metadata.{Obj, Type}
import amf.model.AmfElement
import amf.parser.{AMFASTFactory, ASTEmitter}
import amf.vocabulary.Namespace

import scala.collection.immutable.ListMap

/**
  * AMF Graph emitter
  */
object GraphEmitter {

  def emit(unit: BaseUnit, expanded: Boolean = false): AMFAST = {
    val emitter = Emitter(ASTEmitter(AMFASTFactory()), expanded)
    emitter.root(unit)
  }

  case class Emitter(e: ASTEmitter[AMFToken, AMFAST], expanded: Boolean) {

    private val ctx = context()

    def root(unit: BaseUnit): AMFAST = {
      e.root(Root) { () =>
        map { () =>
          createContextNode()
          traverse(unit, unit.location)
        }
      }
    }

    def traverse(element: AmfElement, parent: String): Unit = {
      val id = element.id(parent)

      createIdNode(element, id)
      createTypeNode(element)

      element.fields.foreach {
        case (f, v) =>
          entry { () =>
            scalar(ctx.reduce(f.value))
            value(f.`type`, v, id)
          }
      }
    }

    private def value(t: Type, v: Value, parent: String) = {
      t match {
        case _: Obj => obj(v.value.asInstanceOf[AmfElement], parent)
        case Str    => scalar(v.value.asInstanceOf[String])
        case RegExp => scalar(v.value.asInstanceOf[String])
        case a: Array =>
          array { () =>
            a.element match {
              case _: Obj => v.value.asInstanceOf[List[AmfElement]].foreach(e => obj(e, parent))
              case Str    => v.value.asInstanceOf[List[String]].foreach(scalar(_))
            }
          }
      }
    }

    private def obj(element: AmfElement, parent: String) = {
      map { () =>
        traverse(element, parent)
      }
    }

    private def scalar(content: String, quoted: Boolean = true) = {
      e.value(StringToken, if (quoted) { content.quote } else content)
    }

    private def createIdNode(element: AmfElement, id: String) = entry("@id", id)

    private def createTypeNode(element: AmfElement) = {
      entry { () =>
        scalar("@type")
        array { () =>
          val obj = metamodel(element)
          obj.`type`.foreach(t => scalar(ctx.reduce(t)))
        }
      }
    }

    private def createContextNode() = {
      if (ctx != EmptyGraphContext) {
        entry { () =>
          scalar("@context")
          map { () =>
            ctx.mappings {
              case (alias, ns) => entry(alias, ns.base)
            }
          }
        }
      }
    }

    private def entry(k: String, v: String): Unit = entry { () =>
      scalar(k)
      scalar(v)
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
