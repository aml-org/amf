package amf.spec.oas

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFToken}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.metadata.domain._
import amf.model.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.parser.Position.ZERO
import amf.parser.{AMFASTFactory, ASTEmitter, Position}
import amf.remote.Oas
import amf.spec.SpecOrdering.ordering
import amf.spec.{Emitter, SpecOrdering}

import scala.collection.mutable

/**
  * Created by pedro.colunga on 8/17/17.
  */
case class OasSpecEmitter(unit: BaseUnit) {

  val emitter: ASTEmitter[AMFToken, AMFAST] = ASTEmitter(AMFASTFactory())

  private def retrieveWebApi() = unit match {
    case document: Document => document.encodes
  }

  def emitWebApi(): AMFAST = {
    val model = retrieveWebApi()
    val api   = WebApiEmitter(model, ordering(Oas, model.annotations))

    emitter.root(Root) { () =>
      map { () =>
        entry { () =>
          raw("swagger")
          raw("2.0")
        }
        traverse(api.emitters)
      }
    }
  }

  private def traverse(emitters: Seq[Emitter]): Unit = {
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
    emitter.value(token, content)
  }

  case class WebApiEmitter(api: WebApi, ordering: SpecOrdering) {
    val emitters: Seq[Emitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[Emitter]()

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Host).map(f => result += ValueEmitter("host", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += RamlParametersEmitter("x-base-uri-parameters", f, ordering))

      fs.entry(WebApiModel.BasePath).map(f => result += ValueEmitter("basePath", f))

      fs.entry(WebApiModel.Accepts)
        .map(f => result += ArrayEmitter("consumes", f, ordering))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += ArrayEmitter("produces", f, ordering))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += ArrayEmitter("schemes", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("contact", f, ordering))

      fs.entry(WebApiModel.Documentation).map(f => result += CreativeWorkEmitter("externalDocs", f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result += EndpointsEmitter("paths", f, ordering))

      ordering.sorted(result)
    }

    private case class InfoEmitter(fs: Fields, ordering: SpecOrdering) extends Emitter {
      override def emit(): Unit = {
        entry { () =>
          raw("info")
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

          fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

          fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

          fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license", f, ordering))

          map { () =>
            traverse(ordering.sorted(result))
          }

        }
      }

      //TODO we lost info node position
      override def position(): Position = {
        var result: Position = ZERO
        fs.entry(WebApiModel.Version)
          .foreach(
            f =>
              f.value.annotations
                .find(classOf[LexicalInformation])
                .foreach({
                  case LexicalInformation(range) => result = range.start
                }))
        fs.entry(WebApiModel.Name)
          .foreach(
            f =>
              f.value.annotations
                .find(classOf[LexicalInformation])
                .foreach({
                  case LexicalInformation(range) =>
                    if (result.isZero || range.start.lt(result)) {
                      result = range.start
                    }
                }))
        result
      }
    }

  }

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val result = mutable.ListBuffer[Emitter]()

          f.value.value
            .asInstanceOf[AmfArray]
            .values
            .foreach(v => {
              result += ScalarEmitter(v.asInstanceOf[AmfScalar])
            })

          array { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class EndPointEmitter(endpoint: EndPoint, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        endpoint.annotations,
        entry { () =>
          val fs = endpoint.fields

          ScalarEmitter(fs.entry(EndPointModel.Path).get.value.value.asInstanceOf[AmfScalar]).emit()

          val result = mutable.ListBuffer[Emitter]()

          fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))

          fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))

          val parameters = endPointParameters(fs)

          //TODO if endpoint and operation has body parameter endpoint looses.
          //TODO we lose name of endpoint parameter with body binding
          if (parameters.nonEmpty)
            result += ParametersEmitter("parameters",
                                        parameters.parameters(),
                                        fs.entry(EndPointModel.UriParameters),
                                        ordering,
                                        parameters.body)

          fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    private def endPointParameters(fs: Fields): EndPointParameters = {
      val ops = fs
        .entry(EndPointModel.Operations)
        .map(operations)
        .getOrElse(Nil)

      var endPointParameters =
        ops
          .filter(_.asInstanceOf[Operation].request != null)
          .foldLeft(EndPointParameters())((parameters, op) =>
            parameters.merge(EndPointParameters(op.asInstanceOf[Operation].request)))

      fs.entry(EndPointModel.UriParameters)
        .foreach(f => {
          val pathParameters = f.value.value.asInstanceOf[AmfArray].values.asInstanceOf[Seq[Parameter]]
          endPointParameters = endPointParameters.merge(EndPointParameters(path = pathParameters))
        })
      endPointParameters
    }

    private def operations(f: FieldEntry, ordering: SpecOrdering): Seq[Emitter] =
      operations(f)
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering))

    private def operations(f: FieldEntry): Seq[AmfElement] = {
      f.value.value
        .asInstanceOf[AmfArray]
        .values
    }

    override def position(): Position = pos(endpoint.annotations)
  }

  case class ParametersEmitter(key: String,
                               parameters: Seq[Parameter],
                               f: Option[FieldEntry],
                               ordering: SpecOrdering,
                               payloadOption: Option[Payload] = None)
      extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        if (f.isDefined) f.get.value.annotations else Annotations(),
        entry { () =>
          raw(key)
          array { () =>
            traverse(parametersMap(parameters, ordering, payloadOption))
          }
        }
      )
    }

    private def parametersMap(parameters: Seq[Parameter],
                              ordering: SpecOrdering,
                              payloadOption: Option[Payload]): Seq[Emitter] = {
      val result = mutable.ListBuffer[Emitter]()
      parameters
        .foreach(e => result += ParameterEmitter(e, ordering))

      //TODO gute review this. Parameter not has media type
      payloadOption.foreach(payload => {
        result += new Emitter {
          override def position(): Position = pos(payload.annotations)

          override def emit(): Unit = {
            map { () =>
              entry { () =>
                raw("in")
                raw("body")
              }

              if (payload.schema != null)
                entry { () =>
                  raw("schema")
                  raw(payload.schema)
                }

              if (payload.mediaType != null)
                entry(() => {
                  raw("x-media-type")
                  raw(payload.mediaType)
                })
            }
          }
        }
      })

      ordering.sorted(result)
    }

    override def position(): Position = {
      if (parameters.nonEmpty) pos(parameters.head.annotations)
      else payloadOption.fold[Position](ZERO)(payload => pos(payload.annotations))
    }
  }

  case class OperationEmitter(operation: Operation, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        operation.annotations,
        entry { () =>
          val fs = operation.fields

          ScalarEmitter(fs.entry(OperationModel.Method).get.value.value.asInstanceOf[AmfScalar]).emit()

          val result = mutable.ListBuffer[Emitter]()

          fs.entry(OperationModel.Name).map(f => result += ValueEmitter("operationId", f))

          fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("deprecated", f, BooleanToken))

          fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))

          fs.entry(OperationModel.Documentation).map(f => result += CreativeWorkEmitter("externalDocs", f, ordering))

          fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("schemes", f, ordering))

          if (operation.request != null)
            result += RequestEmitter(operation.request, ordering)

          fs.entry(OperationModel.Responses).map(f => result += ResponsesEmitter("responses", f, ordering))

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(operation.annotations)
  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
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

    private def responses(f: FieldEntry, ordering: SpecOrdering): Seq[Emitter] = {
      val result = mutable.ListBuffer[Emitter]()
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += ResponseEmitter(e.asInstanceOf[Response], ordering))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ResponseEmitter(response: Response, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        response.annotations,
        entry { () =>
          val result = mutable.ListBuffer[Emitter]()
          val fs     = response.fields

          ScalarEmitter(fs.entry(ResponseModel.Name).get.value.value.asInstanceOf[AmfScalar]).emit()

          fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering))

          val payloads = Payloads(response)
          if (payloads.default.isDefined)
            result += ManualEmitter("x-media-type", payloads.default.get.mediaType)

          if (payloads.payloads.nonEmpty)
            result += PayloadsEmitter("x-response-payloads", payloads.payloads, ordering)

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class RequestEmitter(request: Request, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      val reqFs  = request.fields
      val result = mutable.ListBuffer[Emitter]()

      //TODO gute review this
      val parameters = OperationParameters(request)
      val payloads   = requestPayloads(request)
      if (parameters.nonEmpty || payloads.default.isDefined)
        result += ParametersEmitter("parameters",
                                    parameters.parameters(),
                                    reqFs.entry(RequestModel.QueryParameters),
                                    ordering,
                                    payloads.default) //todo query parameters? header/ what fields should we use?

      if (payloads.payloads.nonEmpty)
        reqFs
          .entry(RequestModel.Payloads)
          .map(f => result += PayloadsEmitter("x-request-payloads", payloads.payloads, ordering))

      traverse(ordering.sorted(result))
    }

    private def requestPayloads(request: Request): Payloads = Payloads(request)

    override def position(): Position = pos(request.annotations)
  }

  case class PayloadsEmitter(key: String, payloads: Seq[Payload], ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      //      sourceOr(
      //        f.value.annotations,
      entry { () =>
        raw(key)

        array { () =>
          val result = mutable.ListBuffer[Emitter]()

          payloads.foreach(p => {
            result += PayloadEmitter(p, ordering)
          })

          traverse(ordering.sorted(result))
        }
      }
      //)
    }

    override def position(): Position = Position.ZERO
  }

  case class PayloadEmitter(payload: Payload, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        payload.annotations,
        //TODO what if payload has no media-type?
        map { () =>
          val fs = payload.fields

          val maybeMediaType = fs.entry(PayloadModel.MediaType)
          if (maybeMediaType.isDefined)
            ValueEmitter("mediaType", maybeMediaType.get).emit()

          val maybeSchema = fs.entry(PayloadModel.Schema)
          if (maybeSchema.isDefined)
            ValueEmitter("schema", maybeSchema.get).emit()

        }
      )
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class RamlParametersEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
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

    private def parameters(f: FieldEntry, ordering: SpecOrdering): Seq[Emitter] = {
      val result = mutable.ListBuffer[Emitter]()
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += RamlParameterEmitter(e.asInstanceOf[Parameter], ordering))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ParameterEmitter(parameter: Parameter, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        parameter.annotations,
        map { () =>
          val result = mutable.ListBuffer[Emitter]()
          val fs     = parameter.fields

          fs.entry(ParameterModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]) || parameter.required)
            .map(f => result += ValueEmitter("required", f, BooleanToken))

          fs.entry(ParameterModel.Binding).map(f => result += ValueEmitter("in", f))

          fs.entry(ParameterModel.Schema).map(f => result += ValueEmitter("type", f))
          //TODO:Schema if body?

          traverse(ordering.sorted(result))

        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class EndpointsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          map { () =>
            traverse(endpoints(f, ordering))
          }
        }
      )
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering): Seq[Emitter] = {
      val result = mutable.ListBuffer[Emitter]()
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += EndPointEmitter(e.asInstanceOf[EndPoint], ordering))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class RamlParameterEmitter(parameter: Parameter, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        parameter.annotations,
        entry { () =>
          val result = mutable.ListBuffer[Emitter]()
          val fs     = parameter.fields

          ScalarEmitter(fs.entry(ParameterModel.Name).get.value.value.asInstanceOf[AmfScalar]).emit()

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("required", f, BooleanToken))

          fs.entry(ParameterModel.Schema).map(f => result += ValueEmitter("type", f))

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class OrganizationEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class CreativeWorkEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.value.value.asInstanceOf[AmfObject].fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))

          map { () =>
            traverse(ordering.sorted(result))
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

  case class ValueEmitter(key: String, f: FieldEntry, token: AMFToken = StringToken) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.value.value.asInstanceOf[AmfScalar].value.toString, token)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ManualEmitter(key: String, value: String, token: AMFToken = StringToken) extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(key)
        raw(value, token)
      }
    }

    override def position(): Position = Position.ZERO
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

  case class EndPointParameters(query: Seq[Parameter] = Nil,
                                path: Seq[Parameter] = Nil,
                                header: Seq[Parameter] = Nil,
                                body: Option[Payload] = None) {

    def merge(parameters: EndPointParameters): EndPointParameters = {
      EndPointParameters(merge(query, parameters.query),
                         merge(path, parameters.path),
                         merge(header, parameters.header),
                         merge(body, parameters.body))
    }

    private def merge(left: Seq[Parameter], right: Seq[Parameter]): Seq[Parameter] =
      (endPointOnly(left) ++ endPointOnly(right)).values.toSeq

    private def merge(left: Option[Payload], right: Option[Payload]): Option[Payload] = left.fold(right)(Some(_))

    private def endPointOnly(left: Seq[Parameter]): Map[String, Parameter] = {
      left.filter(p => p.annotations.contains(classOf[EndPointParameter]) || p.isPath).map(p => p.name -> p).toMap
    }

    def parameters(): Seq[Parameter] = query ++ path ++ header

    def nonEmpty: Boolean = query.nonEmpty || path.nonEmpty || header.nonEmpty || body.isDefined
  }

  object EndPointParameters {
    def apply(request: Request): EndPointParameters = {
      EndPointParameters(request.queryParameters,
                         Nil,
                         request.headers,
                         request.payloads.find(_.annotations.contains(classOf[EndPointBodyParameter])))
    }
  }

  case class OperationParameters(query: Seq[Parameter] = Nil,
                                 header: Seq[Parameter] = Nil,
                                 payload: Option[Payload] = None) {

    def parameters(): Seq[Parameter] = query ++ header

    def nonEmpty: Boolean = query.nonEmpty || header.nonEmpty || payload.isDefined
  }

  object OperationParameters {
    def apply(request: Request): OperationParameters = {
      OperationParameters(
        operationOnly(request.queryParameters),
        operationOnly(request.headers),
        request.payloads.find(_.annotations.contains(classOf[Annotation.OperationBodyParameter]))
      )
    }

    private def operationOnly(left: Seq[Parameter]) = {
      left.filter(!_.annotations.contains(classOf[Annotation.EndPointParameter]))
    }
  }

  case class Payloads(default: Option[Payload] = None, payloads: Seq[Payload] = Nil)

  object Payloads {
    def apply(request: Request): Payloads = {
      val valids       = filterValids(request.payloads)
      val maybePayload = defaultPayload(valids)
      Payloads(maybePayload, nonDefaults(maybePayload, valids))
    }

    def apply(response: Response): Payloads = {
      val valids       = response.payloads
      val maybePayload = defaultPayload(valids)
      Payloads(maybePayload, nonDefaults(maybePayload, valids))
    }

    private def default(all: Seq[Payload]): Option[Payload] = defaultPayload(filterValids(all))

    private def filterValids(all: Seq[Payload]): Seq[Payload] =
      all.filter(p => {
        !p.annotations.contains(classOf[EndPointBodyParameter]) && !p.annotations.contains(
          classOf[OperationBodyParameter])
      })

    private def nonDefaults(default: Option[Payload], valids: Seq[Payload]) =
      if (default.isEmpty) Nil else valids.filter(_ != default.get)

    def defaultPayload(payloads: Seq[Payload]): Option[Payload] = {
      //TODO only for raml?
      if (payloads.isEmpty) None
      else {
        val emptyMTOption = payloads.find(p => p.schema == null || p.schema.isEmpty)
        if (emptyMTOption.isDefined) emptyMTOption
        else {
          val jsonSchemaOption = payloads.find(p => p.mediaType == "application/json")
          if (jsonSchemaOption.isDefined) jsonSchemaOption
          else
            Some(payloads.head)
        }
      }
    }
  }

}
