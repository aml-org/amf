package amf.spec.oas

import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.{CustomDomainProperty, idCounter}
import amf.metadata.Field
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.Position.ZERO
import amf.parser.{ASTEmitter, Position}
import amf.remote.Oas
import amf.shape._
import amf.spec.common.BaseSpecEmitter
import amf.spec.{Declarations, Emitter, SpecOrdering}
import amf.vocabulary.VocabularyMappings
import org.yaml.model.{YDocument, YType}

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * OpenAPI Spec Emitter.
  */
case class OasDocumentEmitter(document: Document) extends OasSpecEmitter {

  private def retrieveWebApi(): WebApi = document.encodes match {
    case webApi: WebApi => webApi
    case _              => throw new Exception("Cannot emit OAS api from document that does not encode a WebAPI")
  }

  def emitDocument(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, document.encodes.annotations)

    val apiEmitters = emitWebApi(ordering)
    // TODO ordering??
    val declares         = DeclarationsEmitter(document.declares, ordering).emitters
    val referenceEmitter = ReferencesEmitter(document.references, ordering)

    emitter.document { () =>
      map { () =>
        entry { () =>
          raw("swagger")
          raw("2.0")
        }
        traverse(ordering.sorted(apiEmitters ++ declares :+ referenceEmitter))
      }
    }
  }

  def emitWebApi(ordering: SpecOrdering): Seq[Emitter] = {
    val model = retrieveWebApi()
    val api   = WebApiEmitter(model, ordering)
    api.emitters
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

      result ++= OasAnnotationsEmitter(api, ordering).emitters

      ordering.sorted(result)
    }

    private case class InfoEmitter(fs: Fields, ordering: SpecOrdering) extends Emitter {
      override def emit(): Unit = {
        val result = mutable.ListBuffer[Emitter]()

        fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

        fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

        fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

        fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license", f, ordering))

        if (result.nonEmpty)
          entry { () =>
            raw("info")
            map { () =>
              traverse(ordering.sorted(result))
            }
          }

      }

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

  case class EndPointEmitter(endpoint: EndPoint, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        endpoint.annotations,
        entry { () =>
          val fs = endpoint.fields

          ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit()

          val result = mutable.ListBuffer[Emitter]()

          fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))

          fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("x-", f, ordering).emitters())

          val parameters = endPointParameters()

          if (parameters.nonEmpty)
            result += ParametersEmitter("parameters", parameters.parameters(), ordering, parameters.body)

          fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering, parameters.body.isDefined))

          result ++= OasAnnotationsEmitter(endpoint, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    private def endPointParameters(): EndPointParameters =
      endpoint.operations
        .filter(op => Option(op.request).isDefined)
        .foldLeft(EndPointParameters(path = endpoint.parameters))((parameters, op) =>
          parameters.merge(EndPointParameters(op.request)))

    private def operations(f: FieldEntry, ordering: SpecOrdering, endpointPayloadEmitted: Boolean): Seq[Emitter] =
      f.array.values
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering, endpointPayloadEmitted))

    override def position(): Position = pos(endpoint.annotations)
  }

  case class ParametersEmitter(key: String,
                               parameters: Seq[Parameter],
                               ordering: SpecOrdering,
                               payloadOption: Option[Payload] = None)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(key)
        array { () =>
          traverse(parameters(ordering))
        }
      }
    }

    private def parameters(ordering: SpecOrdering): Seq[Emitter] = {
      val result = mutable.ListBuffer[Emitter]()
      parameters.foreach(e => result += ParameterEmitter(e, ordering))

      payloadOption.foreach(payload => result += PayloadAsParameterEmitter(payload, ordering))

      ordering.sorted(result)
    }

    override def position(): Position = {
      if (parameters.nonEmpty) pos(parameters.head.annotations)
      else payloadOption.fold[Position](ZERO)(payload => pos(payload.annotations))
    }
  }

  case class PayloadAsParameterEmitter(payload: Payload, ordering: SpecOrdering) extends Emitter {
    override def position(): Position = pos(payload.annotations)

    override def emit(): Unit = {
      map { () =>
        val result = mutable.ListBuffer[Emitter]()

        payload.fields
          .entry(PayloadModel.Schema)
          .map(f => result += SchemaEmitter(f, ordering))

        payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))

        result += EntryEmitter("in", "body")

        result ++= OasAnnotationsEmitter(payload, ordering).emitters

        traverse(ordering.sorted(result))
      }
    }
  }

  case class OperationEmitter(operation: Operation, ordering: SpecOrdering, endpointPayloadEmitted: Boolean)
      extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        operation.annotations,
        entry { () =>
          val fs = operation.fields

          ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit()

          val result = mutable.ListBuffer[Emitter]()

          fs.entry(OperationModel.Name).map(f => result += ValueEmitter("operationId", f))

          fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("deprecated", f, YType.Bool))

          fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))

          fs.entry(OperationModel.Documentation).map(f => result += CreativeWorkEmitter("externalDocs", f, ordering))

          fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("schemes", f, ordering))

          fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("x-", f, ordering).emitters())

          Option(operation.request).foreach(req => result ++= requestEmitters(req, ordering, endpointPayloadEmitted))

          fs.entry(OperationModel.Responses).map(f => result += ResponsesEmitter("responses", f, ordering))

          result ++= OasAnnotationsEmitter(operation, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(operation.annotations)

    def requestEmitters(request: Request, ordering: SpecOrdering, endpointPayloadEmitted: Boolean): Seq[Emitter] = {

      val result = mutable.ListBuffer[Emitter]()

      val parameters = operationOnly(request.queryParameters) ++ operationOnly(request.headers)
      val payloads   = Payloads(request.payloads, endpointPayloadEmitted)

      if (parameters.nonEmpty || payloads.default.isDefined)
        result += ParametersEmitter("parameters", parameters, ordering, payloads.default)

      if (payloads.other.nonEmpty) result += PayloadsEmitter("x-request-payloads", payloads.other, ordering)

      result ++= OasAnnotationsEmitter(request, ordering).emitters

      result
    }

    private def operationOnly(parameters: Seq[Parameter]) =
      parameters.filter(!_.annotations.contains(classOf[Annotation.EndPointParameter]))

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
      f.array.values
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

          ScalarEmitter(fs.entry(ResponseModel.Name).get.scalar).emit()

          fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering))

          val payloads = Payloads(response.payloads)

          payloads.default.foreach(payload => {
            payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))
            payload.fields
              .entry(PayloadModel.Schema)
              .map(f => result += SchemaEmitter(f, ordering))
          })

          if (payloads.other.nonEmpty)
            result += PayloadsEmitter("x-response-payloads", payloads.other, ordering)

          result ++= OasAnnotationsEmitter(response, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class PayloadsEmitter(key: String, payloads: Seq[Payload], ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
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
    }

    override def position(): Position = {
      val filtered = payloads
        .filter(p => p.annotations.find(classOf[LexicalInformation]).exists(!_.range.start.isZero))
      val result = filtered
        .foldLeft[Position](ZERO)(
          (pos, p) =>
            p.annotations
              .find(classOf[LexicalInformation])
              .map(_.range.start)
              .filter(newPos => pos.isZero || pos.lt(newPos))
              .getOrElse(pos))
      result
    }
  }

  case class PayloadEmitter(payload: Payload, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        payload.annotations,
        map { () =>
          val fs     = payload.fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType", f))

          fs.entry(PayloadModel.Schema).map(f => result += SchemaEmitter(f, ordering))

          result ++= OasAnnotationsEmitter(payload, ordering).emitters

          traverse(ordering.sorted(result))
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
      f.array.values
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
            .map(f => result += ValueEmitter("required", f, YType.Bool))

          fs.entry(ParameterModel.Binding).map(f => result += ValueEmitter("in", f))

          fs.entry(ParameterModel.Schema)
            .map(f =>
              result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape], ordering, Seq(ShapeModel.Description))
                .emitters())

          result ++= OasAnnotationsEmitter(parameter, ordering).emitters

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
      f.array.values
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

          ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit()

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("required", f, YType.Bool))

          fs.entry(ParameterModel.Schema)
            .map(f =>
              result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape], ordering, Seq(ShapeModel.Description))
                .emitters())

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

          val fs     = f.obj.fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

          result ++= OasAnnotationsEmitter(f.domainElement, ordering).emitters

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

          val fs     = f.obj.fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

          result ++= OasAnnotationsEmitter(f.domainElement, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
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

  case class Payloads(default: Option[Payload], other: Seq[Payload])

  object Payloads {
    def apply(payloads: Seq[Payload], endpointPayloadEmitted: Boolean = false): Payloads = {
      val clean = payloads.filter(!_.annotations.contains(classOf[EndPointBodyParameter]))

      var default = clean.find(_.annotations.contains(classOf[DefaultPayload]))

      default = if (endpointPayloadEmitted) default else default.orElse(defaultPayload(clean))

      Payloads(default, clean.filter(_ != default.orNull))
    }

    def defaultPayload(payloads: Seq[Payload]): Option[Payload] =
      payloads
        .find(p => Option(p.mediaType).isEmpty || p.mediaType.isEmpty)
        .orElse(payloads.find(_.mediaType == "application/json"))
        .orElse(payloads.headOption)
  }

}

class OasSpecEmitter extends BaseSpecEmitter {
  val emitter = ASTEmitter()

  case class ReferencesEmitter(references: Seq[BaseUnit], ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      val modules = references.collect({ case m: Module => m })
      if (modules.nonEmpty) {
        entry { () =>
          raw("x-uses")
          map { () =>
            idCounter.reset()
            traverse(ordering.sorted(references.map(r => ReferenceEmitter(r, ordering, idCounter.genId("uses")))))
          }
        }
      }
    }

    override def position(): Position = Position.ZERO
  }

  case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, alias: String) extends Emitter {

    override def emit(): Unit = EntryEmitter(alias, reference.id).emit()

    override def position(): Position = Position.ZERO

  }

  case class DeclarationsEmitter(declares: Seq[DomainElement], ordering: SpecOrdering) {
    val emitters: Seq[Emitter] = {
      val declarations = Declarations(declares)

      val result = ListBuffer[Emitter]()

      if (declarations.shapes.nonEmpty) result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, ordering)

      if (declarations.annotations.nonEmpty)
        result += AnnotationsTypesEmitter(declarations.annotations.values.toSeq, ordering)

      if (declarations.resourceTypes.nonEmpty)
        result += AbstractDeclarationsEmitter("x-resourceTypes", declarations.resourceTypes.values.toSeq, ordering)

      if (declarations.traits.nonEmpty)
        result += AbstractDeclarationsEmitter("x-traits", declarations.traits.values.toSeq, ordering)

      result
    }
  }

  case class DeclaredTypesEmitters(types: Seq[Shape], ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw("definitions")
        map { () =>
          traverse(ordering.sorted(types.map(t => NamedTypeEmitter(t, ordering))))
        }
      }
    }

    override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
  }

  case class NamedTypeEmitter(shape: Shape, ordering: SpecOrdering) extends Emitter {
    override def position(): Position = pos(shape.annotations)

    override def emit(): Unit = {
      entry { () =>
        val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
        raw(name)
        if (shape.linkTarget.isDefined)
          shape.linkTarget.foreach(l => TagToReferenceEmitter(l, shape.linkLabel).emit())
        map { () =>
          traverse(ordering.sorted(OasTypeEmitter(shape, ordering).emitters()))
        }
      }
    }
  }

  case class TagToReferenceEmitter(linkTarget: DomainElement with Linkable, label: Option[String]) {
    def emit(): Unit = {
      val refVal = label.getOrElse(linkTarget.id)
      map { () =>
        ref(refVal)
      }
    }
  }

  protected def ref(url: String): Unit = EntryEmitter("$ref", url).emit() // todo YType("$ref")

  case class AnnotationsTypesEmitter(properties: Seq[CustomDomainProperty], ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw("x-annotationTypes")
        map { () =>
          traverse(ordering.sorted(properties.map(p => NamedPropertyTypeEmitter(p, ordering))))
        }
      }
    }

    override def position(): Position = properties.headOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO)
  }

  case class NamedPropertyTypeEmitter(annotationType: CustomDomainProperty, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        val name = Option(annotationType.name)
          .orElse(throw new Exception(s"Cannot declare annotation type without name $annotationType"))
          .get
        raw(name)
        map { () =>
          val emitters = AnnotationTypeEmitter(annotationType, ordering).emitters()
          traverse(ordering.sorted(emitters))
        }
      }

    }

    override def position(): Position = pos(annotationType.annotations)
  }

  case class OasTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil) {
    def emitters(): Seq[Emitter] = {
      shape match {
        case node: NodeShape =>
          val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
          NodeShapeEmitter(copiedNode, ordering).emitters()
        case array: ArrayShape =>
          val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
          ArrayShapeEmitter(copiedArray, ordering).emitters()
        case nil: NilShape =>
          val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
          Seq(NilShapeEmitter(copiedNil, ordering))
        case scalar: ScalarShape =>
          val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
          ScalarShapeEmitter(copiedScalar, ordering).emitters()
        case _ => Seq()
      }
    }
  }

  abstract class ShapeEmitter(shape: Shape, ordering: SpecOrdering) {
    def emitters(): Seq[Emitter] = {

      val result = ListBuffer[Emitter]()
      val fs     = shape.fields

      fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("title", f))

      fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(ShapeModel.Default).map(f => result += ValueEmitter("default", f))

      fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

      fs.entry(ShapeModel.Documentation).map(f => result += CreativeWorkEmitter("externalDocs", f, ordering))

      fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

      result ++= OasAnnotationsEmitter(shape, ordering).emitters

      result
    }
  }

  case class ArrayShapeEmitter(shape: ArrayShape, ordering: SpecOrdering) {
    def emitters(): Seq[Emitter] = {
      val result = ListBuffer[Emitter]()
      val fs     = shape.fields

      result += EntryEmitter("type", "array")

      result += ItemsShapeEmitter(shape, ordering)

      fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

      fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

      fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

      result ++= OasAnnotationsEmitter(shape, ordering).emitters

      result
    }
  }

  case class ItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering) extends Emitter {
    def emit(): Unit = {
      entry { () =>
        raw("items")
        OasTypeEmitter(array.items, ordering).emitters().foreach(_.emit())
      }
    }

    override def position(): Position = pos(array.items.fields.getValue(ArrayShapeModel.Items).annotations)
  }

  case class XMLSerializerEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.obj.fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(XMLSerializerModel.Attribute)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("attribute", f))

          fs.entry(XMLSerializerModel.Wrapped)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("wrapped", f))

          fs.entry(XMLSerializerModel.Name)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("name", f))

          fs.entry(XMLSerializerModel.Namespace).map(f => result += ValueEmitter("namespace", f))

          fs.entry(XMLSerializerModel.Prefix).map(f => result += ValueEmitter("prefix", f))

          result ++= OasAnnotationsEmitter(f.domainElement, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class NodeShapeEmitter(node: NodeShape, ordering: SpecOrdering) extends ShapeEmitter(node, ordering) {
    override def emitters(): Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer[Emitter]() ++ super.emitters()

      val fs = node.fields

      // TODO annotation for original position?
      if (node.annotations.contains(classOf[ExplicitField]))
        result += EntryEmitter("type", "object")

      fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

      fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

      fs.entry(NodeShapeModel.Closed)
        .filter(_.value.annotations.contains(classOf[ExplicitField]))
        .map(f =>
          result += EntryEmitter("additionalProperties", (!node.closed).toString, position = pos(f.value.annotations)))

      fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

      fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("x-discriminator-value", f))

      fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("readOnly", f))

      // TODO required array.

      fs.entry(NodeShapeModel.Properties).map(f => result += PropertiesShapeEmitter(f, ordering))

      val propertiesMap = ListMap(node.properties.map(p => p.id -> p): _*)

      fs.entry(NodeShapeModel.Dependencies).map(f => result += ShapeDependenciesEmitter(f, ordering, propertiesMap))

      fs.entry(NodeShapeModel.Inherits).map(f => result += ShapeInheritsEmitter(f, ordering))

      result
    }

  }

  case class ShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      val inherits = f.array.values.map(_.asInstanceOf[Shape])
      entry(() => {
        raw("allOf")

        array(() => inherits.foreach(emitInherit))

      })

    }

    private def emitInherit(shape: Shape): Unit = {
      map { () =>
        if (shape.annotations.contains(classOf[DeclaredElement])) inlineEmit(shape)
        else declaredEmit(shape)
      }
    }

    def inlineEmit(shape: Shape): Unit = {
      traverse(ordering.sorted(OasTypeEmitter(shape, ordering).emitters()))
    }

    def declaredEmit(shape: Shape): Unit = {
      raw("$ref")
      raw("#/definitions/" + shape.name)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ShapeDependenciesEmitter(f: FieldEntry,
                                      ordering: SpecOrdering,
                                      propertiesMap: ListMap[String, PropertyShape])
      extends Emitter {
    def emit(): Unit = {

      entry { () =>
        raw("dependencies")
        map { () =>
          val result = f.array.values.map(v =>
            PropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, propertiesMap))
          traverse(ordering.sorted(result))
        }
      }
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PropertyDependenciesEmitter(property: PropertyDependencies,
                                         ordering: SpecOrdering,
                                         properties: ListMap[String, PropertyShape])
      extends Emitter {

    def emit(): Unit = {
      properties
        .get(property.propertySource)
        .foreach(p => {
          entry { () =>
            raw(p.name)

            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name, iri.annotations)))
              })

            targets.foreach(t => {
              array { () =>
                traverse(ordering.sorted(t.map(ScalarEmitter)))
              }
            })
          }
        })
    }

    override def position(): Position = pos(property.annotations) // TODO check this
  }

  case class NilShapeEmitter(nil: NilShape, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit =
      entry { () =>
        raw("type")
        raw("null")
      }


    override def position(): Position = pos(nil.annotations)
  }

  case class ScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering) extends ShapeEmitter(scalar, ordering) {
    override def emitters(): Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer[Emitter]() ++ super.emitters()

      val fs = scalar.fields

      val typeDef = OasTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(scalar.dataType)) // TODO Check this

      fs.entry(ScalarShapeModel.DataType)
        .map(
          f =>
            result += EntryEmitter(
              "type",
              typeDef,
              position =
                if (f.value.annotations.contains(classOf[Inferred])) Position.ZERO
                else pos(f.value.annotations))) // TODO check this  - annotations of typeDef in parser

      fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

      fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

      fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

      fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum", f))

      fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum", f))

      fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("exclusiveMinimum", f))

      fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("exclusiveMaximum", f))

      fs.entry(ScalarShapeModel.Format).map(f => result += ValueEmitter("format", f))

      fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf", f))

      result
    }
  }

  case class PropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    def emit(): Unit = {

      entry { () =>
        raw("properties")
        map { () =>
          val result = f.array.values.map(v => PropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering))
          traverse(ordering.sorted(result))
        }
      }
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering) extends Emitter {

    def emit(): Unit = {
      entry { () =>
        raw(property.name)
        map { () =>
          traverse(ordering.sorted(OasTypeEmitter(property.range, ordering).emitters()))
        }
      }
    }

    override def position(): Position = pos(property.annotations) // TODO check this
  }

  case class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering) {
    def emitters(): Seq[Emitter] = {
      val result = ListBuffer[Emitter]()
      val fs     = property.fields

      fs.entry(CustomDomainPropertyModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

      fs.entry(CustomDomainPropertyModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(CustomDomainPropertyModel.Domain).map { f =>
        val scalars = f.array.scalars.map { s =>
          VocabularyMappings.uriToRaml.get(s.toString) match {
            case Some(identifier) => AmfScalar(identifier, s.annotations)
            case None             => s
          }
        }
        val finalArray      = AmfArray(scalars, f.array.annotations)
        val finalFieldEntry = FieldEntry(f.field, Value(finalArray, f.value.annotations))

        if (f.value.annotations.contains(classOf[SingleValueArray]))
          result += ArrayValueEmitter("allowedTargets", finalFieldEntry)
        else result += ArrayEmitter("allowedTargets", finalFieldEntry, ordering)
      }

      fs.entry(CustomDomainPropertyModel.Schema)
        .map({ f =>
          result += SchemaEmitter(f, ordering)
        })

      result ++= OasAnnotationsEmitter(property, ordering).emitters

      result
    }
  }

  case class SchemaEmitter(f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      val shape = f.value.value.asInstanceOf[Shape]

      entry { () =>
        raw("schema")
        map { () =>
          val emitters = OasTypeEmitter(shape, ordering).emitters()
          traverse(ordering.sorted(emitters))
        }
      }
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class CreativeWorkEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val fs     = f.obj.fields
          val result = mutable.ListBuffer[Emitter]()

          fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))

          fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))

          result ++= OasAnnotationsEmitter(f.domainElement, ordering).emitters

          map { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  // todo to check, extention?
  case class UserDocumentationEmitter(userDocumentation: UserDocumentation, ordering: SpecOrdering) extends Emitter {

    override def emit(): Unit = {
      val result = ListBuffer[Emitter]()
      val fs     = userDocumentation.fields
      fs.entry(UserDocumentationModel.Title).map(f => result += ValueEmitter("title", f))
      fs.entry(UserDocumentationModel.Content).map(f => result += ValueEmitter("content", f))

      map { () =>
        traverse(ordering.sorted(result))
      }
    }

    override def position(): Position = pos(userDocumentation.annotations)
  }

}
