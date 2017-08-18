package amf.spec.oas

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFToken}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.metadata.Field
import amf.metadata.domain._
import amf.model.{AmfArray, AmfObject, AmfScalar}
import amf.parser.Position.ZERO
import amf.parser.{AMFASTFactory, ASTEmitter, Position}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by pedro.colunga on 8/17/17.
  */
case class OasSpecEmitter(unit: BaseUnit) {

  val emitter: ASTEmitter[AMFToken, AMFAST] = ASTEmitter(AMFASTFactory())

  private def retrieveWebApi() = unit match {
    case document: Document => document.encodes
  }

  def emitWebApi(): AMFAST = {
    val api = WebApiEmitter(retrieveWebApi(), Lexical)

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
    emitter.value(token, content)
  }

  case class WebApiEmitter(api: WebApi, ordering: Ordering[Emitter]) {
    val emitters: mutable.SortedSet[Emitter] = {
      val fs     = api.fields
      val result = mutable.SortedSet()(ordering)

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Host).map(f => result += ValueEmitter("host", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += EndpointsEmitter("x-base-uri-parameters", f, ordering))

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

      result
    }

    private case class InfoEmitter(fs: Fields, ordering: Ordering[Emitter]) extends Emitter {
      override def emit(): Unit = {
        entry { () =>
          raw("info")
          val result = mutable.SortedSet()(ordering)

          fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

          fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

          fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

          fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license", f, ordering))

          map { () =>
            traverse(result)
          }

        }
      }

      //TODO we lost info node position
      override def position(): Position = Position.ZERO
    }
  }

  trait Emitter {
    def emit(): Unit
    def position(): Position
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

          var paramForUp                     = ListBuffer[Parameter]()
          var payloadOption: Option[Payload] = None

          //TODO add search for operations parameters that comes from endpoint parameters with binding query/header

          //TODO what if i had only one endpoint parameters and has been overrided by and operation parameter??

          //TODO gute review this
          fs.entry(EndPointModel.Operations)
            .map(_.value.value.asInstanceOf[AmfArray].values)
            .map(_.asInstanceOf[Seq[Operation]])
            .getOrElse(Nil)
            .foreach(o => {
              paramForUp = filterParamsForUp(o.request.fields, paramForUp)
              payloadOption = findPayload(o, payloadOption)
            })
          //TODO gute review this

          buildParameters(fs, paramForUp).map(f =>
            result += ParametersEmitter("parameters", f, ordering, payloadOption))

          fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

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
    //TODO gute review this

    def plainParam(field: Field, reqFields: Fields): Seq[Parameter] = {
      reqFields
        .entry(field)
        .map(_.value.value.asInstanceOf[AmfArray].values)
        .map(_.asInstanceOf[Seq[Parameter]])
        .getOrElse(Nil)
    }
    //TODO gute review this

    def filterParamsForUp(reqFields: Fields, foundedParams: ListBuffer[Parameter]): ListBuffer[Parameter] = {

      (plainParam(RequestModel.Headers, reqFields) ++ plainParam(RequestModel.QueryParameters, reqFields))
        .filter(_.annotations.contains(classOf[EndPointParameter]))
        .foreach(p => {
          if (!foundedParams.map(_.name).contains(p.name))
            foundedParams += p
        })
      foundedParams
    }
    //TODO gute review this

    def findPayload(o: Operation, payloadOption: Option[Payload]): Option[Payload] = {
      if (payloadOption.isDefined) payloadOption
      else {
        val payloadsOption: Option[Seq[Payload]] = o.request.fields
          .entry(RequestModel.Payloads)
          .map(_.value.value.asInstanceOf[AmfArray].values.map(_.asInstanceOf[Payload]))
        payloadsOption.getOrElse(Nil).find(p => p.annotations.contains(classOf[EndPointBodyParameter]))
      }
    }
    //TODO gute review this

    def buildParameters(fs: Fields, paramsForUp: ListBuffer[Parameter]): Option[FieldEntry] = {
      fs.entry(EndPointModel.UriParameters)
        .map(f => {
          val newValue = f.value.copy(
            value = AmfArray(f.value.value.asInstanceOf[AmfArray].values ++ paramsForUp)
          )
          f.copy(value = newValue)
        })
    }
  }

  case class ParametersEmitter(key: String,
                               f: FieldEntry,
                               ordering: Ordering[Emitter],
                               payloadOption: Option[Payload] = None)
      extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          array { () =>
            traverse(parameters(f, ordering, payloadOption))
          }
        }
      )
    }

    private def parameters(f: FieldEntry,
                           ordering: Ordering[Emitter],
                           payloadOption: Option[Payload] = None): mutable.SortedSet[Emitter] = {
      val result = mutable.SortedSet()(ordering)
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += ParameterEmitter(e.asInstanceOf[Parameter], ordering))

      //TODO gute review this

      if (payloadOption.isDefined) {
        result += new Emitter {
          override def position(): Position = Position.ZERO

          override def emit(): Unit = {
            map { () =>
              entry { () =>
                raw("in")
                raw("body")
              }

              if (payloadOption.get.schema != null)
                entry { () =>
                  raw("type")
                  raw(payloadOption.get.schema)
                }

              if (payloadOption.get.mediaType != null)
                entry(() => {
                  raw("x-media-type")
                  raw(payloadOption.get.mediaType)
                })
            }
          }
        }
      }
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class OperationEmitter(operation: Operation, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        operation.annotations,
        entry { () =>
          val fs = operation.fields

          ScalarEmitter(fs.entry(OperationModel.Method).get.value.value.asInstanceOf[AmfScalar]).emit()

          val result = mutable.SortedSet()(ordering)

          fs.entry(OperationModel.Name).map(f => result += ValueEmitter("operationId", f))

          fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("deprecated", f, BooleanToken))

          fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))

          fs.entry(OperationModel.Documentation).map(f => result += CreativeWorkEmitter("externalDocs", f, ordering))

          fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("schemes", f, ordering))

          val reqFs = operation.request.fields

          //TODO gute review this
          val maybeParameter = reqFs
            .entry(RequestModel.Payloads)
            .find(_.value.value.annotations.contains(classOf[OperationBodyParameter]))
            .map(_.value.value.asInstanceOf[Payload])
          //TODO filter endpoints parameters

          unifyParameters(reqFs).map(f => result += ParametersEmitter("parameters", f, ordering, maybeParameter))

          reqFs.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("x-request-payloads", f, ordering))

          fs.entry(OperationModel.Responses).map(f => result += ResponsesEmitter("responses", f, ordering))

          map { () =>
            traverse(result)
          }
        }
      )
    }

    override def position(): Position = pos(operation.annotations)

    //TODO gute review this
    def unifyParameters(reqFs: Fields): Iterable[FieldEntry] = {
      (reqFs
        .entry(RequestModel.QueryParameters)
        .filter(!_.value.annotations.contains(classOf[EndPointParameter]))
        ++
          reqFs
            .entry(RequestModel.Headers)
            .filter(!_.value.annotations.contains(classOf[EndPointParameter])))
    }
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

          ScalarEmitter(fs.entry(ResponseModel.Name).get.value.value.asInstanceOf[AmfScalar]).emit()

          fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering))

          fs.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("body", f, ordering))

          //TODO x-response-payloads
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
      //TODO gute review this

      //TODO where goes this aux method?
      def defaultPayload(payloads: Seq[Payload]): Option[Payload] = {
        //TODO only for raml?
        //            val payloads: Seq[Payload] = payloadsArray.values.map(_.asInstanceOf[Payload])
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

      val values: Seq[Payload] = f.value.value
        .asInstanceOf[AmfArray]
        .values
        .map(_.asInstanceOf[Payload])

      val defPayload = defaultPayload(values)
      val result     = mutable.SortedSet()(ordering)

      values
        .filter(
          v =>
            !v.annotations.contains(classOf[OperationBodyParameter]) || !v.annotations.contains(
              classOf[EndPointBodyParameter]))
        .filter(v => v != defPayload.orNull)
        .foreach(e => result += PayloadEmitter(e, ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PayloadEmitter(payload: Payload, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        payload.annotations,
        //TODO what if payload has no media-type?
        entry { () =>
          val fs = payload.fields

          ScalarEmitter(fs.entry(PayloadModel.MediaType).get.value.value.asInstanceOf[AmfScalar]).emit()

          ScalarEmitter(fs.entry(PayloadModel.Schema).get.value.value.asInstanceOf[AmfScalar]).emit()
        }
      )
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class RamlParametersEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
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
        .foreach(e => result += RamlParameterEmitter(e.asInstanceOf[Parameter], ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ParameterEmitter(parameter: Parameter, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        parameter.annotations,
        map { () =>
          val result = mutable.SortedSet()(ordering)
          val fs     = parameter.fields

          fs.entry(ParameterModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .map(f => result += ValueEmitter("required", f, BooleanToken))

          fs.entry(ParameterModel.Binding).map(f => result += ValueEmitter("in", f))

          fs.entry(ParameterModel.Schema).map(f => result += ValueEmitter("type", f))
          //TODO:Schema if body?

          traverse(result)

        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class EndpointsEmitter(key: String, f: FieldEntry, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value.annotations,
        entry { () =>
          raw(key)

          map { () =>
            traverse(endpointers(f, ordering))
          }
        }
      )
    }

    private def endpointers(f: FieldEntry, ordering: Ordering[Emitter]): mutable.SortedSet[Emitter] = {
      val result = mutable.SortedSet()(ordering)
      f.value.value
        .asInstanceOf[AmfArray]
        .values
        .foreach(e => result += EndPointEmitter(e.asInstanceOf[EndPoint], ordering))
      result
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class RamlParameterEmitter(parameter: Parameter, ordering: Ordering[Emitter]) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        parameter.annotations,
        entry { () =>
          val result = mutable.SortedSet()(ordering)
          val fs     = parameter.fields

          ScalarEmitter(fs.entry(ParameterModel.Name).get.value.value.asInstanceOf[AmfScalar]).emit()

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

  case class ValueEmitter(key: String, f: FieldEntry, token: AMFToken = StringToken) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.value.value.asInstanceOf[AmfScalar].value.toString, token)
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
    //TODO gute review this(for final remove)

    //    annotations
//      .find(classOf[SourceAST])
//      .fold(inner)(a => emitter.addChild(a.ast))
    inner
  }

  object Default extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = 1
  }

  object Lexical extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = x.position().compareTo(y.position())
  }

}
