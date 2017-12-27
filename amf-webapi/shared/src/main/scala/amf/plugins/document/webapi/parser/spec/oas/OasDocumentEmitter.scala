package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.model.document._
import amf.core.model.domain._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, EmptyFutureDeclarations, FieldEntry, Fields, Position, Value}
import amf.core.remote.{Oas, Vendor}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.IdCounter
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, ScalarShape}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models._
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * OpenAPI Spec Emitter.
  */
case class OasDocumentEmitter(document: BaseUnit) extends OasSpecEmitter {

  private def retrieveWebApi(): WebApi = document match {
    case document: Document => document.encodes.asInstanceOf[WebApi]
    case _                  => throw new Exception("BaseUnit doesn't encode a WebApi.")
  }

  def extensionEmitter(): Seq[EntryEmitter] =
    document.fields
      .entry(ExtensionLikeModel.Extends)
      .map(f => NamedRefEmitter("x-extends", f.scalar.toString, pos = pos(f.value.annotations)))
      .toList ++ retrieveHeader()

  private def retrieveHeader() = document match {
    case _: Extension => Some(MapEntryEmitter(Oas20Extension.tuple))
    case _: Overlay   => Some(MapEntryEmitter(Oas20Overlay.tuple))
    case _: Document  => None
    case _            => throw new Exception("Document has no header.")
  }

  def emitDocument(): YDocument = {
    val doc = document.asInstanceOf[Document]

    val ordering = SpecOrdering.ordering(Oas, doc.encodes.annotations)

    val references = ReferencesEmitter(document.references, ordering)
    val declares   = DeclarationsEmitter(doc.declares, ordering, references.references).emitters
    val api        = emitWebApi(ordering, references.references)
    val extension  = extensionEmitter()
    val usage: Option[ValueEmitter] =
      doc.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    YDocument {
      _.obj { b =>
        b.swagger = "2.0"
        traverse(ordering.sorted(api ++ extension ++ usage ++ declares :+ references), b)
      }
    }
  }

  def emitWebApi(ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val model  = retrieveWebApi()
    val vendor = model.annotations.find(classOf[SourceVendor]).map(_.vendor)
    val api    = WebApiEmitter(model, ordering, vendor, references)
    api.emitters
  }

  case class WebApiEmitter(api: WebApi, ordering: SpecOrdering, vendor: Option[Vendor], references: Seq[BaseUnit]) {
    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Host).map(f => result += ValueEmitter("host", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += Raml10ParametersEmitter("x-base-uri-parameters", f, ordering, Nil))

      fs.entry(WebApiModel.BasePath).map(f => result += ValueEmitter("basePath", f))

      fs.entry(WebApiModel.Accepts)
        .map(f => result += ArrayEmitter("consumes", f, ordering, force = true))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += ArrayEmitter("produces", f, ordering, force = true))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += ArrayEmitter("schemes", f, ordering))

      fs.entry(WebApiModel.Documentations).map(f => result ++= UserDocumentationsEmitter(f, ordering).emitters())

      fs.entry(WebApiModel.EndPoints)
        .fold(result += EntryPartEmitter("paths", EmptyMapEmitter()))(f =>
          result += EndpointsEmitter("paths", f, ordering, references))

      fs.entry(WebApiModel.Security).map(f => result += ParametrizedSecuritiesSchemeEmitter("security", f, ordering))

      result ++= AnnotationsEmitter(api, ordering).emitters

      ordering.sorted(result)
    }

    private case class InfoEmitter(fs: Fields, ordering: SpecOrdering) extends EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        val result = mutable.ListBuffer[EntryEmitter]()

        fs.entry(WebApiModel.Name)
          .fold(result += MapEntryEmitter("title", "API"))(f => result += ValueEmitter("title", f))

        fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

        fs.entry(WebApiModel.Version)
          .fold(result += MapEntryEmitter("version", "1.0"))(f => result += ValueEmitter("version", f))

        fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license", f, ordering))

        fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("contact", f, ordering))

        b.entry(
          "info",
          _.obj(traverse(ordering.sorted(result), _))
        )
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

  case class EndPointEmitter(endpoint: EndPoint, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = endpoint.fields
      sourceOr(
        endpoint.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit(_),
          _.obj { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("x-displayName", f))
            fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("x-description", f))
            fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("x-", f, ordering).emitters())

            val parameters = endPointParameters()

            if (parameters.nonEmpty)
              result ++= ParametersEmitter("parameters",
                                           parameters.parameters(),
                                           ordering,
                                           parameters.body,
                                           references)
                .emitters()

            fs.entry(EndPointModel.Operations)
              .map(f => result ++= operations(f, ordering, parameters.body.isDefined, references))

            fs.entry(EndPointModel.Security)
              .map(f => result += ParametrizedSecuritiesSchemeEmitter("x-security", f, ordering))

            result ++= AnnotationsEmitter(endpoint, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    private def endPointParameters(): EndPointParameters =
      endpoint.operations
        .filter(op => Option(op.request).isDefined)
        .foldLeft(EndPointParameters(path = endpoint.parameters))((parameters, op) =>
          parameters.merge(EndPointParameters(op.request)))

    private def operations(f: FieldEntry,
                           ordering: SpecOrdering,
                           endpointPayloadEmitted: Boolean,
                           references: Seq[BaseUnit]): Seq[EntryEmitter] =
      f.array.values
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering, endpointPayloadEmitted, references))

    override def position(): Position = pos(endpoint.annotations)
  }

  case class OperationEmitter(operation: Operation,
                              ordering: SpecOrdering,
                              endpointPayloadEmitted: Boolean,
                              references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = operation.fields

      sourceOr(
        operation.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
          _.obj { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(OperationModel.Name).map(f => result += ValueEmitter("operationId", f))
            fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))
            fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("deprecated", f))
            fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))
            fs.entry(OperationModel.Documentation)
              .map(
                f =>
                  result += OasEntryCreativeWorkEmitter("externalDocs",
                                                        f.value.value.asInstanceOf[CreativeWork],
                                                        ordering))
            fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("schemes", f, ordering))
            fs.entry(OperationModel.Accepts).map(f => result += ArrayEmitter("consumes", f, ordering))
            fs.entry(OperationModel.ContentType).map(f => result += ArrayEmitter("produces", f, ordering))
            fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("x-", f, ordering).emitters())
            Option(operation.request).foreach(req =>
              result ++= requestEmitters(req, ordering, endpointPayloadEmitted, references))
            fs.entry(OperationModel.Responses)
              .fold(result += EntryPartEmitter("responses", EmptyMapEmitter()))(f =>
                result += ResponsesEmitter("responses", f, ordering, references))

            fs.entry(OperationModel.Security)
              .map(f => result += ParametrizedSecuritiesSchemeEmitter("security", f, ordering))

            result ++= AnnotationsEmitter(operation, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(operation.annotations)

    def requestEmitters(request: Request,
                        ordering: SpecOrdering,
                        endpointPayloadEmitted: Boolean,
                        references: Seq[BaseUnit]): Seq[EntryEmitter] = {

      val result = mutable.ListBuffer[EntryEmitter]()

      val parameters = operationOnly(request.queryParameters) ++ operationOnly(request.headers)
      val payloads   = Payloads(request.payloads, endpointPayloadEmitted)

      if (parameters.nonEmpty || payloads.default.isDefined)
        result ++= ParametersEmitter("parameters", parameters, ordering, payloads.default, references).emitters()

      if (payloads.other.nonEmpty)
        result += PayloadsEmitter("x-request-payloads", payloads.other, ordering, references)

      val fs = request.fields

      fs.entry(RequestModel.QueryString)
        .map { f =>
          Option(f.value.value) match {
            case Some(shape: AnyShape) =>
              result += RamlNamedTypeEmitter(shape, ordering, Nil, Raml10TypePartEmitter.apply)
            case Some(_) => throw new Exception("Cannot emit a non WebApi Shape")
            case None    => // ignore
          }
        }

      fs.entry(RequestModel.BaseUriParameters)
        .map { f =>
          result += Raml08ParametersEmitter("x-baseUriParameters", f, ordering, references)
        }

      result ++= AnnotationsEmitter(request, ordering).emitters

      result
    }

    private def operationOnly(parameters: Seq[Parameter]) =
      parameters.filter(!_.annotations.contains(classOf[EndPointParameter]))

  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.obj(traverse(responses(f, ordering), _))
        )
      )
    }

    private def responses(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      ordering.sorted(f.array.values.map(e => ResponseEmitter(e.asInstanceOf[Response], ordering, references)))
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = response.fields

      sourceOr(
        response.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(ResponseModel.Name).get.scalar).emit(_),
          _.obj { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(ResponseModel.Description)
              .orElse(Some(FieldEntry(ResponseModel.Description, Value(AmfScalar(""), Annotations())))) // this is mandatory in OAS 2.0
              .map(f => result += ValueEmitter("description", f))
            fs.entry(RequestModel.Headers)
              .map(f => result += Raml10ParametersEmitter("headers", f, ordering, references))

            val payloads = Payloads(response.payloads)

            payloads.default.foreach(payload => {
              payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))
              payload.fields
                .entry(PayloadModel.Schema)
                .map { f =>
                  if (!f.value.value.annotations.contains(classOf[SynthesizedField])) {
                    result += OasSchemaEmitter(f, ordering, references)
                  }
                }
            })

            if (payloads.other.nonEmpty)
              result += PayloadsEmitter("x-response-payloads", payloads.other, ordering, references)

            fs.entry(ResponseModel.Examples).map(f => result += OasResponseExamplesEmitter("examples", f, ordering))
            result ++= AnnotationsEmitter(response, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class PayloadsEmitter(key: String, payloads: Seq[Payload], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        _.list(traverse(ordering.sorted(payloads.map(p => PayloadEmitter(p, ordering, references))), _))
      )
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

  case class PayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit]) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      sourceOr(
        payload.annotations,
        b.obj { b =>
          val fs     = payload.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType", f))
          fs.entry(PayloadModel.Schema).map { f =>
            if (!f.value.value.annotations.contains(classOf[SynthesizedField])) {
              result += OasSchemaEmitter(f, ordering, references)
            }
          }

          result ++= AnnotationsEmitter(payload, ordering).emitters

          traverse(ordering.sorted(result), b)
        }
      )
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class EndpointsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.obj(traverse(endpoints(f, ordering, references), _))
        )
      )
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
      val result = f.array.values.map(e => EndPointEmitter(e.asInstanceOf[EndPoint], ordering, references))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          _.obj { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

            result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class OrganizationEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          _.obj { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))
            fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

            result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
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

  override implicit val spec: SpecEmitterContext = OasSpecEmitterContext

  case class ReferencesEmitter(references: Seq[BaseUnit], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val modules = references.collect({ case m: Module => m })
      if (modules.nonEmpty) {
        val idCounter: IdCounter = new IdCounter
        b.entry(
          "x-uses",
          _.obj { b =>
            traverse(
              ordering.sorted(references.map(r => ReferenceEmitter(r, ordering, () => idCounter.genId("uses")))),
              b)
          }
        )
      }
    }

    override def position(): Position = ZERO
  }

  case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, aliasGenerator: () => String)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val aliases = reference.annotations.find(classOf[Aliases])

      def entry(tuple: (String, String)): Unit = tuple match {
        case (alias, path) =>
          val ref = path match {
            case "" => reference.id
            case _  => path
          }
          MapEntryEmitter(alias, ref).emit(b)
      }

      aliases.fold {
        entry(aliasGenerator() -> "")
      } { _ =>
        aliases.foreach(_.aliases.foreach(entry))
      }
    }

    override def position(): Position = ZERO
  }

  case class DeclarationsEmitter(declares: Seq[DomainElement], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends PlatformSecrets {
    val emitters: Seq[EntryEmitter] = {

      val declarations = WebApiDeclarations(declares, None, EmptyFutureDeclarations())

      val result = ListBuffer[EntryEmitter]()

      if (declarations.shapes.nonEmpty)
        result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, ordering, references)

      if (declarations.annotations.nonEmpty)
        result += AnnotationsTypesEmitter(declarations.annotations.values.toSeq, ordering)

      if (declarations.resourceTypes.nonEmpty)
        result += AbstractDeclarationsEmitter("x-resourceTypes",
                                              declarations.resourceTypes.values.toSeq,
                                              ordering,
                                              Nil)

      if (declarations.traits.nonEmpty)
        result += AbstractDeclarationsEmitter("x-traits", declarations.traits.values.toSeq, ordering, Nil)

      if (declarations.securitySchemes.nonEmpty)
        result += OasSecuritySchemesEmitters(declarations.securitySchemes.values.toSeq, ordering)

      if (declarations.parameters.nonEmpty)
        result += DeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering, references)

      result
    }
  }

  case class DeclaredTypesEmitters(types: Seq[Shape], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("definitions",
              _.obj(traverse(ordering.sorted(types.map(OasNamedTypeEmitter(_, ordering, references))), _)))
    }

    override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }

  case class DeclaredParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "parameters",
        _.obj(traverse(ordering.sorted(parameters.map(NamedParameterEmitter(_, ordering, references))), _))
      )
    }

    override def position(): Position = parameters.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
  }

  case class NamedParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def position(): Position = pos(parameter.annotations)

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        Option(parameter.name).getOrElse(throw new Exception(s"Cannot declare shape without name $parameter")),
        b => {
          if (parameter.isLink) OasTagToReferenceEmitter(parameter, parameter.linkLabel).emit(b)
          else ParameterEmitter(parameter, ordering, references).emit(b)
        }
      )
    }
  }

  case class NamedRefEmitter(key: String, url: String, pos: Position = ZERO) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        spec.ref(_, url)
      )
    }

    override def position(): Position = pos
  }

  case class AnnotationsTypesEmitter(properties: Seq[CustomDomainProperty], ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("x-annotationTypes",
              _.obj(traverse(ordering.sorted(properties.map(NamedPropertyTypeEmitter(_, ordering))), _)))
    }

    override def position(): Position = properties.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
  }

  case class NamedPropertyTypeEmitter(annotationType: CustomDomainProperty, ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        Option(annotationType.name)
          .orElse(throw new Exception(s"Cannot declare annotation type without name $annotationType"))
          .get,
        b => {
          if (annotationType.isLink) OasTagToReferenceEmitter(annotationType, annotationType.linkLabel).emit(b)
          else
            AnnotationTypeEmitter(annotationType, ordering).emitters() match {
              case Left(emitters) =>
                b.obj { b =>
                  traverse(ordering.sorted(emitters), b)
                }
              case Right(part) => part.emit(b)
            }

        }
      )
    }

    def emitAnnotationFields(): Unit = {}

    override def position(): Position = pos(annotationType.annotations)
  }

  case class UserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {

      val documents: List[CreativeWork] = f.array.values.collect({ case c: CreativeWork => c }).toList

      documents match {
        case head :: Nil => Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering))
        case head :: tail =>
          Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering), RamlCreativeWorkEmitters(tail, ordering))
        case _ => Nil
      }

    }
  }

  case class RamlCreativeWorkEmitters(documents: Seq[CreativeWork], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "x-user-documentation",
        _.list(
          traverse(ordering.sorted(documents.map(RamlCreativeWorkEmitter(_, ordering, withExtension = false))), _))
      )
    }

    override def position(): Position = pos(documents.head.annotations)
  }

  case class ParametersEmitter(key: String,
                               parameters: Seq[Parameter],
                               ordering: SpecOrdering,
                               payloadOption: Option[Payload] = None,
                               references: Seq[BaseUnit]) {
    def emitters(): Seq[EntryEmitter] = {

      val results = ListBuffer[EntryEmitter]()
      val (oasParameters, ramlParameters) =
        parameters.partition(p => Option(p.schema).isEmpty || p.schema.isInstanceOf[ScalarShape])

      if (oasParameters.nonEmpty || payloadOption.isDefined)
        results += OasParameterEmitter(oasParameters, references)

      if (ramlParameters.nonEmpty) {
        val query  = ramlParameters.filter(_.isQuery)
        val header = ramlParameters.filter(_.isHeader)
        if (query.nonEmpty)
          results += XRamlParameterEmitter("x-queryParameters", query)
        if (header.nonEmpty)
          results += XRamlParameterEmitter("x-headers", header)
      }
      results
    }

    case class OasParameterEmitter(oasParameters: Seq[Parameter], references: Seq[BaseUnit]) extends EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        if (oasParameters.nonEmpty || payloadOption.isDefined)
          b.entry(
            key,
            _.list(traverse(parameters(oasParameters, ordering, references), _))
          )
      }

      override def position(): Position =
        oasParameters.headOption
          .map(p => pos(p.annotations))
          .getOrElse(payloadOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO))
    }

    case class XRamlParameterEmitter(key: String, ramlParameters: Seq[Parameter]) extends EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        if (ramlParameters.nonEmpty)
          b.entry(
            key,
            _.obj(traverse(ramlParameters.map(p => Raml10ParameterEmitter(p, ordering, Nil)), _))
          )
      }

      override def position(): Position =
        ramlParameters.headOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO)
    }

    private def parameters(parameters: Seq[Parameter],
                           ordering: SpecOrdering,
                           references: Seq[BaseUnit]): Seq[PartEmitter] = {
      val result = ListBuffer[PartEmitter]()
      parameters.foreach(e => result += ParameterEmitter(e, ordering, references))
      payloadOption.foreach(payload => result += PayloadAsParameterEmitter(payload, ordering, references))
      ordering.sorted(result)
    }

  }

  case class ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      sourceOr(
        parameter.annotations,
        if (parameter.isLink) {
          spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(parameter.linkLabel.get))
        } else {
          val result = mutable.ListBuffer[EntryEmitter]()
          val fs     = parameter.fields

          fs.entry(ParameterModel.Name).map(f => result += ValueEmitter("name", f))

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]) || parameter.required)
            .map(f => result += ValueEmitter("required", f))

          fs.entry(ParameterModel.Binding).map(f => result += ValueEmitter("in", f))

          fs.entry(ParameterModel.Schema)
            .map(
              f =>
                result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape],
                                          ordering,
                                          Seq(ShapeModel.Description),
                                          references)
                  .entries())

          result ++= AnnotationsEmitter(parameter, ordering).emitters

          b.obj(traverse(ordering.sorted(result), _))
        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class PayloadAsParameterEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends PartEmitter {

    override def emit(b: PartBuilder): Unit = {
      b.obj { b =>
        val result = mutable.ListBuffer[EntryEmitter]()

        payload.fields
          .entry(PayloadModel.Schema)
          .map(f => result += OasSchemaEmitter(f, ordering, references))

        payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))

        result += MapEntryEmitter("in", "body")

        result ++= AnnotationsEmitter(payload, ordering).emitters

        traverse(ordering.sorted(result), b)
      }
    }

    override def position(): Position = pos(payload.annotations)
  }

}

object OasSpecEmitterContext extends SpecEmitterContext {
  override def ref(b: PartBuilder, url: String): Unit = OasRefEmitter(url).emit(b)

  override val vendor: Vendor = Oas

  override def localReference(reference: Linkable): PartEmitter =
    OasTagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel)

  override val tagToReferenceEmitter = new WebApiTagToReferenceEmitter(Oas)
}
