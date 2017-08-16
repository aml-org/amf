package amf.graph

import amf.common.AMFToken._
import amf.common.Strings.strings
import amf.common._
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.metadata.Type.{Array, Bool, Iri, RegExp, Str}
import amf.metadata.document.DocumentModel
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.domain._
import amf.metadata.{Obj, SourceMapModel, Type}
import amf.model.AmfObject
import amf.parser.{AMFASTFactory, ASTEmitter}
import amf.vocabulary.Namespace.SourceMaps
import amf.vocabulary.ValueType

/**
  * AMF Graph emitter
  */
object GraphEmitter {

  def emit(unit: BaseUnit): AMFAST = {
    val emitter = Emitter(ASTEmitter(AMFASTFactory()))
    emitter.root(unit)
  }

  case class Emitter(emitter: ASTEmitter[AMFToken, AMFAST]) {

    def root(unit: BaseUnit): AMFAST = {
      emitter.root(Root) { () =>
        array { () =>
          map { () =>
            traverse(unit, unit.location)
          }
        }
      }
    }

    def traverse(element: AmfObject, parent: String): Unit = {
      val id = element.id
      createIdNode(id)

      val obj = metamodel(element)
      createTypeNode(obj)

      val sources = SourceMap(id, element)

      obj.fields.map(element.fields.entry).foreach {
        case Some((f, v)) =>
          entry { () =>
            val url = f.value.iri()
            raw(url)
            value(f.`type`, v, id, sources.property(url))
          }
        case None => // Missing field
      }

      createSourcesNode(id + "/source-map", sources)
    }

    private def value(t: Type, v: Value, parent: String, sources: (Value) => Unit) = {
      t match {
        case _: Obj => obj(v.value.asInstanceOf[AmfObject], parent)
        case Iri =>
          iri(v.value.asInstanceOf[String])
          sources(v)
        case Str | RegExp =>
          scalar(v.value.asInstanceOf[String])
          sources(v)
        case Bool =>
          scalar(v.value.asInstanceOf[Boolean].toString, BooleanToken)
          sources(v)
        case a: Array =>
          array { () =>
            a.element match {
              case _: Obj => v.value.asInstanceOf[Seq[AmfObject]].foreach(e => obj(e, parent, inArray = true))
              case Str    => v.value.asInstanceOf[Seq[String]].foreach(scalar(_, inArray = true))
            }
          }
      }
    }

    private def obj(element: AmfObject, parent: String, inArray: Boolean = false) = {
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
      emitter.value(token, if (token == StringToken) {
        content.quote
      } else content)
    }

    private def iri(content: String): Unit = {
      array { () =>
        map { () =>
          entry { () =>
            raw("@id")
            raw(content)
          }
        }
      }
    }

    private def scalar(content: String, token: AMFToken = StringToken, inArray: Boolean = false): Unit = {
      if (inArray) {
        value(content, token)
      } else {
        array { () =>
          value(content, token)
        }
      }
    }

    private def value(content: String, token: AMFToken) = {
      map { () =>
        entry { () =>
          raw("@value")
          raw(content, token)
        }
      }
    }

    private def createIdNode(id: String) = entry("@id", id)

    private def createTypeNode(obj: Obj) = {
      entry { () =>
        raw("@type")
        array { () =>
          obj.`type`.foreach(t => raw(t.iri()))
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
      emitter.beginNode()
      inner()
      emitter.endNode(t)
    }

    private def createSourcesNode(id: String, sources: SourceMap): Unit = {
      if (sources.nonEmpty) {
        entry { () =>
          raw(Sources.value.iri())
          array { () =>
            map { () =>
              createIdNode(id)
              createTypeNode(SourceMapModel)
              createAnnotationNodes(sources)
            }
          }
        }
      }
    }

    private def createAnnotationNodes(sources: SourceMap) = {
      sources.annotations.foreach({
        case (a, values) =>
          entry { () =>
            raw(ValueType(SourceMaps, a).iri())
            array { () =>
              values.foreach(createAnnotationValueNode)
            }
          }
      })
    }

    private def createAnnotationValueNode(tuple: (String, String)): Unit = tuple match {
      case (iri, v) =>
        map { () =>
          entry { () =>
            raw(SourceMapModel.Element.value.iri())
            scalar(iri)
          }
          entry { () =>
            raw(SourceMapModel.Value.value.iri())
            scalar(v)
          }
        }
    }
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
