package amf.spec.oas

import amf.document.Fragment.{ExtensionFragment, OverlayFragment}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.{CustomDomainProperty, idCounter}
import amf.metadata.domain._
import amf.metadata.shape._
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.remote.{Oas, Vendor}
import amf.shape._
import amf.spec._
import amf.spec.common.BaseEmitters._
import amf.spec.common._
import amf.spec.declaration._
import amf.spec.domain.{ParametrizedSecuritiesSchemeEmitter, RamlParametersEmitter}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * OpenAPI Spec Emitter.
  */
case class OasDocumentEmitter(document: BaseUnit) extends OasSpecEmitter {

  private def retrieveWebApi(): WebApi = document match {
    case document: Document           => document.encodes.asInstanceOf[WebApi]
    case extension: ExtensionFragment => extension.encodes
    case overlay: OverlayFragment     => overlay.encodes
    case _                            => throw new Exception("BaseUnit doesn't encode a WebApi.")
  }

  def emitDocument(): YDocument = {
    val doc = document.asInstanceOf[Document]

    val ordering = SpecOrdering.ordering(Oas, doc.encodes.annotations)

    val api        = emitWebApi(ordering)
    val declares   = DeclarationsEmitter(doc.declares, ordering).emitters
    val references = ReferencesEmitter(document.references, ordering)

    YDocument {
      _.map { b =>
        b.entry("swagger", "2.0")
        traverse(ordering.sorted(api ++ declares :+ references), b)
      }
    }
  }

  def emitWebApi(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val model  = retrieveWebApi()
    val vendor = model.annotations.find(classOf[SourceVendor]).map(_.vendor)
    val api    = WebApiEmitter(model, ordering, vendor)
    api.emitters
  }

  case class WebApiEmitter(api: WebApi, ordering: SpecOrdering, vendor: Option[Vendor]) {
    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Host).map(f => result += ValueEmitter("host", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += RamlParametersEmitter("x-base-uri-parameters", f, ordering, Nil))

      fs.entry(WebApiModel.BasePath).map(f => result += ValueEmitter("basePath", f))

      fs.entry(WebApiModel.Accepts)
        .map(f => result += ArrayEmitter("consumes", f, ordering, force = true))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += ArrayEmitter("produces", f, ordering, force = true))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += ArrayEmitter("schemes", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("contact", f, ordering))

      fs.entry(WebApiModel.Documentations).map(f => result ++= UserDocumentationsEmitter(f, ordering).emitters())

      fs.entry(WebApiModel.EndPoints).map(f => result += EndpointsEmitter("paths", f, ordering))

      fs.entry(WebApiModel.Security).map(f => result += ParametrizedSecuritiesSchemeEmitter("security", f, ordering))

      result ++= AnnotationsEmitter(api, ordering).emitters

      ordering.sorted(result)
    }

    private case class InfoEmitter(fs: Fields, ordering: SpecOrdering) extends EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        val result = mutable.ListBuffer[EntryEmitter]()

        fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

        fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService", f))

        fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

        fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license", f, ordering))

        if (result.nonEmpty)
          b.entry(
            "info",
            _.map(traverse(ordering.sorted(result), _))
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

  case class EndPointEmitter(endpoint: EndPoint, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = endpoint.fields
      sourceOr(
        endpoint.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit(_),
          _.map { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))
            fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))
            fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("x-", f, ordering).emitters())

            val parameters = endPointParameters()

            if (parameters.nonEmpty)
              result += ParametersEmitter("parameters", parameters.parameters(), ordering, parameters.body)

            fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering, parameters.body.isDefined))

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

    private def operations(f: FieldEntry, ordering: SpecOrdering, endpointPayloadEmitted: Boolean): Seq[EntryEmitter] =
      f.array.values
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering, endpointPayloadEmitted))

    override def position(): Position = pos(endpoint.annotations)
  }

  case class OperationEmitter(operation: Operation, ordering: SpecOrdering, endpointPayloadEmitted: Boolean)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = operation.fields

      sourceOr(
        operation.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
          _.map { b =>
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

            Option(operation.request).foreach(req => result ++= requestEmitters(req, ordering, endpointPayloadEmitted))

            fs.entry(OperationModel.Responses).map(f => result += ResponsesEmitter("responses", f, ordering))

            fs.entry(OperationModel.Security)
              .map(f => result += ParametrizedSecuritiesSchemeEmitter("security", f, ordering))

            result ++= AnnotationsEmitter(operation, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(operation.annotations)

    def requestEmitters(request: Request, ordering: SpecOrdering, endpointPayloadEmitted: Boolean): Seq[EntryEmitter] = {

      val result = mutable.ListBuffer[EntryEmitter]()

      val parameters = operationOnly(request.queryParameters) ++ operationOnly(request.headers)
      val payloads   = Payloads(request.payloads, endpointPayloadEmitted)

      if (parameters.nonEmpty || payloads.default.isDefined)
        result += ParametersEmitter("parameters", parameters, ordering, payloads.default)

      if (payloads.other.nonEmpty) result += PayloadsEmitter("x-request-payloads", payloads.other, ordering)

      result ++= AnnotationsEmitter(request, ordering).emitters

      result
    }

    private def operationOnly(parameters: Seq[Parameter]) =
      parameters.filter(!_.annotations.contains(classOf[Annotation.EndPointParameter]))

  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.map(traverse(responses(f, ordering), _))
        )
      )
    }

    private def responses(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      ordering.sorted(f.array.values.map(e => ResponseEmitter(e.asInstanceOf[Response], ordering)))
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ResponseEmitter(response: Response, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = response.fields

      sourceOr(
        response.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(ResponseModel.Name).get.scalar).emit(_),
          _.map { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))
            fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering, Nil))

            val payloads = Payloads(response.payloads)

            payloads.default.foreach(payload => {
              payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))
              payload.fields
                .entry(PayloadModel.Schema)
                .map(f => result += OasSchemaEmitter(f, ordering))
            })

            if (payloads.other.nonEmpty)
              result += PayloadsEmitter("x-response-payloads", payloads.other, ordering)

            result ++= AnnotationsEmitter(response, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class PayloadsEmitter(key: String, payloads: Seq[Payload], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        _.list(traverse(ordering.sorted(payloads.map(p => PayloadEmitter(p, ordering))), _))
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

  case class PayloadEmitter(payload: Payload, ordering: SpecOrdering) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      sourceOr(
        payload.annotations,
        b.map { b =>
          val fs     = payload.fields
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType", f))
          fs.entry(PayloadModel.Schema).map(f => result += OasSchemaEmitter(f, ordering))

          result ++= AnnotationsEmitter(payload, ordering).emitters

          traverse(ordering.sorted(result), b)
        }
      )
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class EndpointsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.map(b => traverse(endpoints(f, ordering), b))
        )
      )
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      val result = f.array.values.map(e => EndPointEmitter(e.asInstanceOf[EndPoint], ordering))
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
          _.map { b =>
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
          _.map { b =>
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
        b.entry(
          "x-uses",
          _.map { b =>
            idCounter.reset()
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
      val alias = reference.annotations.find(classOf[Aliases])

      def entry(alias: String) = MapEntryEmitter(alias, reference.id).emit(b)

      alias.fold {
        entry(aliasGenerator())
      } { _ =>
        alias.foreach(_.aliases.foreach(entry))
      }
    }

    override def position(): Position = ZERO
  }

  case class DeclarationsEmitter(declares: Seq[DomainElement], ordering: SpecOrdering) {
    val emitters: Seq[EntryEmitter] = {
      val declarations = Declarations(declares)

      val result = ListBuffer[EntryEmitter]()

      if (declarations.shapes.nonEmpty) result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, ordering)

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
        result += DeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering)

      result
    }
  }

  case class DeclaredTypesEmitters(types: Seq[Shape], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("definitions", _.map { b =>
        traverse(ordering.sorted(types.map(NamedTypeEmitter(_, ordering))), b)
      })
    }

    override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }

  case class DeclaredParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "parameters",
        _.map(traverse(ordering.sorted(parameters.map(NamedParameterEmitter(_, ordering))), _))
      )
    }

    override def position(): Position = parameters.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
  }

  case class NamedTypeEmitter(shape: Shape, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
      b.entry(name, OasTypePartEmitter(shape, ordering).emit(_))
    }

    override def position(): Position = pos(shape.annotations)
  }

  case class NamedParameterEmitter(parameter: Parameter, ordering: SpecOrdering) extends EntryEmitter {
    override def position(): Position = pos(parameter.annotations)

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        Option(parameter.name).getOrElse(throw new Exception(s"Cannot declare shape without name $parameter")),
        b => {
          if (parameter.isLink) OasTagToReferenceEmitter(parameter, parameter.linkLabel).emit(b)
          else ParameterEmitter(parameter, ordering).emit(b)
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
      b.entry("x-annotationTypes", _.map { b =>
        traverse(ordering.sorted(properties.map(NamedPropertyTypeEmitter(_, ordering))), b)
      })
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
            b.map { b =>
              val emitters = AnnotationTypeEmitter(annotationType, ordering).emitters()
              traverse(ordering.sorted(emitters), b)
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
                               payloadOption: Option[Payload] = None)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        _.list(traverse(parameters(ordering), _))
      )
    }

    private def parameters(ordering: SpecOrdering): Seq[PartEmitter] = {
      val result = ListBuffer[PartEmitter]()
      parameters.foreach(e => result += ParameterEmitter(e, ordering))
      payloadOption.foreach(payload => result += PayloadAsParameterEmitter(payload, ordering))
      ordering.sorted(result)
    }

    override def position(): Position = {
      if (parameters.nonEmpty) pos(parameters.head.annotations)
      else payloadOption.fold[Position](ZERO)(payload => pos(payload.annotations))
    }
  }

  case class ParameterEmitter(parameter: Parameter, ordering: SpecOrdering) extends PartEmitter {
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
            .map(f =>
              result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape], ordering, Seq(ShapeModel.Description))
                .entries())

          result ++= AnnotationsEmitter(parameter, ordering).emitters

          b.map(traverse(ordering.sorted(result), _))
        }
      )
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class PayloadAsParameterEmitter(payload: Payload, ordering: SpecOrdering) extends PartEmitter {

    override def emit(b: PartBuilder): Unit = {
      b.map { b =>
        val result = mutable.ListBuffer[EntryEmitter]()

        payload.fields
          .entry(PayloadModel.Schema)
          .map(f => result += OasSchemaEmitter(f, ordering))

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
}
