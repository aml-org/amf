package amf.spec.raml

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFToken}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.{ExplicitField, LexicalInformation, SynthesizedField}
import amf.domain._
import amf.maker.BaseUriSplitter
import amf.metadata.domain._
import amf.model.{AmfArray, AmfObject, AmfScalar}
import amf.parser.Position.ZERO
import amf.parser.{AMFASTFactory, ASTEmitter, Position}
import amf.remote.Raml
import amf.spec.Emitter
import amf.spec.SpecOrdering.ordering

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
    val model = retrieveWebApi()
    val api   = WebApiEmitter(model, ordering(Raml, model.annotations))

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
      //      api.endPoints.find(_.path.contains("/levelzero/level-one")).get.operations.head.request.queryParameters.find(_.name.contains("param1")).get.withDescription("Some descr changed")
      //      api.endPoints.find(_.path.contains("/levelzero/level-one")).get.operations.head.responses.find(_.statusCode=="200").get.headers.head.withSchema("invented")

      val fs     = api.fields
      val result = mutable.SortedSet()(ordering)

      fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

      fs.entry(WebApiModel.BaseUriParameters).map(f => result += ParametersEmitter("baseUriParameters", f, ordering))

      fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(WebApiModel.ContentType).map(f => result += ArrayEmitter("mediaType", f, ordering))

      fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

      fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("(termsOfService)", f))

      fs.entry(WebApiModel.Schemes)
        .filter(!_.value.annotations.contains(classOf[SynthesizedField]))
        .map(f => result += ArrayEmitter("protocols", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("(contact)", f, ordering))

      fs.entry(WebApiModel.Documentation).map(f => result += CreativeWorkEmitter("(externalDocs)", f, ordering))

      fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("(license)", f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result ++= endpoints(f, ordering))

      //TODO source not available for baseUri node
      result += BaseUriEmitter(fs)

      result
    }

    private def endpoints(f: FieldEntry, ordering: Ordering[Emitter]): Seq[Emitter] = {
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .map(e => EndPointEmitter(e.asInstanceOf[EndPoint], ordering))
    }

    private case class BaseUriEmitter(fs: Fields) extends Emitter {
      override def emit(): Unit = {
        val protocol: String = fs
          .entry(WebApiModel.Schemes)
          .find(_.value.annotations.contains(classOf[SynthesizedField]))
          .flatMap(_.value.value.asInstanceOf[AmfArray].values.headOption)
          .map(_.asInstanceOf[AmfScalar].value.toString)
          .getOrElse("")

        val domain: String = fs
          .entry(WebApiModel.Host)
          .map(_.value.value.asInstanceOf[AmfScalar].value)
          .map(_.toString)
          .getOrElse("")

        val basePath: String = fs
          .entry(WebApiModel.BasePath)
          .map(_.value.value.asInstanceOf[AmfScalar].value)
          .map(_.toString)
          .getOrElse("")

        val uri = BaseUriSplitter(protocol, domain, basePath)

        if (uri.nonEmpty) {
          entry { () =>
            raw("baseUri")
            raw(uri.url())
          }
        }
      }

      //TODO position not available for baseUri node
      override def position(): Position = Position.ZERO
    }

  }

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
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

          fs.entry(EndPointModel.UriParameters).map(f => result += ParametersEmitter("uriParameters", f, ordering))

          fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

          //TODO endpoint nesting.

          map { () =>
            traverse(result)
          }
        }
      )
    }

    private def operations(f: FieldEntry, ordering: Ordering[Emitter]): Seq[Emitter] = {
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering))
    }

    override def position(): Position = pos(endpoint.annotations)
  }

  case class OperationEmitter(operation: Operation, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        operation.annotations,
        entry { () =>
          val fs = operation.fields

          ScalarEmitter(fs.entry(OperationModel.Method).get.value.value.asInstanceOf[AmfScalar]).emit()

          val result = mutable.SortedSet()(ordering)

          fs.entry(OperationModel.Name).map(f => result += ValueEmitter("displayName", f))

          fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("(deprecated)", f))

          fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("(summary)", f))

          fs.entry(OperationModel.Documentation).map(f => result += CreativeWorkEmitter("(externalDocs)", f, ordering))

          fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("protocols", f, ordering))

          if (operation.request != null) {
            val reqFs = operation.request.fields

            reqFs
              .entry(RequestModel.QueryParameters)
              .map(f => result += ParametersEmitter("queryParameters", f, ordering))

            reqFs.entry(RequestModel.Headers).map(f => result += ParametersEmitter("headers", f, ordering))

            reqFs.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("body", f, ordering))

          }

          fs.entry(OperationModel.Responses).map(f => result += ResponsesEmitter("responses", f, ordering))
          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(operation.annotations)
  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          map { () =>
            traverse(responses(f, ordering))
          }
        }
      )
    }

    private def responses(f: FieldEntry, ordering: Ordering[Emitter]): mutable.SortedSet[Emitter] = {
      val result = mutable.SortedSet()(ordering)
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += ResponseEmitter(e.asInstanceOf[Response], ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ResponseEmitter(response: Response, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        response.annotations,
        entry { () =>
          val result = mutable.SortedSet()(ordering)
          val fs     = response.fields

          ScalarEmitter(fs.entry(ResponseModel.StatusCode).get.value.value.asInstanceOf[AmfScalar]).emit()

          fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(RequestModel.Headers).map(f => result += ParametersEmitter("headers", f, ordering))

          fs.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("body", f, ordering))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class PayloadsEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          map { () =>
            traverse(payloads(f, ordering))
          }
        }
      )
    }

    private def payloads(f: FieldEntry, ordering: Ordering[Emitter]): mutable.SortedSet[Emitter] = {
      val result = mutable.SortedSet()(ordering)
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += PayloadEmitter(e.asInstanceOf[Payload], ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PayloadEmitter(payload: Payload, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        payload.annotations, {
          val fs = payload.fields
          //TODO what if payload has no media-type?
          fs.entry(PayloadModel.MediaType)
            .fold(
              ValueEmitter("type", fs.entry(PayloadModel.Schema).get).emit()
            )(meditaType => {
              entry { () =>
                ScalarEmitter(meditaType.value.value.asInstanceOf[AmfScalar]).emit()
                ScalarEmitter(fs.entry(PayloadModel.Schema).get.value.value.asInstanceOf[AmfScalar]).emit()
              }
            })
        }
      )
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class ParametersEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          map { () =>
            traverse(parameters(f, ordering))
          }
        }
      )
    }

    private def parameters(f: FieldEntry, ordering: Ordering[Emitter]): mutable.SortedSet[Emitter] = {
      val result = mutable.SortedSet()(ordering)
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += ParameterEmitter(e.asInstanceOf[Parameter], ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ParameterEmitter(parameter: Parameter, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        parameter.annotations,
        entry { () =>
          val fs = parameter.fields

          val explicit = fs
            .entry(ParameterModel.Required)
            .exists(_.value.annotations.contains(classOf[ExplicitField]))

          if (!explicit && !parameter.required) {
            ScalarEmitter(AmfScalar(parameter.name + "?")).emit()
          } else {
            val name = fs.entry(ParameterModel.Name).get.value.value.asInstanceOf[AmfScalar]
            ScalarEmitter(name).emit()
          }

          val result = mutable.SortedSet()(ordering)

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("required", f))

          fs.entry(ParameterModel.Schema).map(f => result += ValueEmitter("type", f))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.SortedSet()(ordering)

          fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class OrganizationEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.SortedSet()(ordering)

          fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class CreativeWorkEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.SortedSet()(ordering)

          fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))

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
    //TODO first lvl gets sources and changes in the children doesn't matter.
    //    annotations
    //      .find(classOf[SourceAST])
    //      .fold(inner)(a => emitter.addChild(a.ast))
    inner
  }
}
