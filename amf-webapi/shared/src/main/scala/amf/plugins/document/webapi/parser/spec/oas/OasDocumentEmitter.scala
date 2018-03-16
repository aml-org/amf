package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document._
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.Position.ZERO
import amf.core.parser.{EmptyFutureDeclarations, FieldEntry, Fields, Position}
import amf.core.remote.{Oas, Vendor}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.{BaseSpecEmitter, OasSpecEmitterContext}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.IdCounter
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models._
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * OpenAPI Spec Emitter.
  */
case class OasDocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasSpecEmitter {

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
        .map(f => result += RamlParametersEmitter("x-base-uri-parameters", f, ordering, Nil)(toRaml(spec)))

      fs.entry(WebApiModel.BasePath).map(f => result += ValueEmitter("basePath", f))

      fs.entry(WebApiModel.Accepts)
        .map(f => result += ArrayEmitter("consumes", f, ordering, force = true))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += ArrayEmitter("produces", f, ordering, force = true))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += ArrayEmitter("schemes", f, ordering))

      fs.entry(WebApiModel.Documentations).map(f => result ++= UserDocumentationsEmitter(f, ordering).emitters())

      // Annotations collected from the "paths" element that has no direct representation in any model element
      // They will be passed to the EndpointsEmitter
      val orphanAnnotations = api.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

      fs.entry(WebApiModel.EndPoints)
        .fold(result += EntryPartEmitter("paths", EmptyMapEmitter()))(f =>
          result += EndpointsEmitter("paths", f, ordering, references, orphanAnnotations))

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

            val parameters = Parameters.classified(endpoint.path, endpoint.parameters, endpoint.payloads.headOption)

            if (parameters.nonEmpty)
              result ++= OasParametersEmitter("parameters",
                                              parameters.query ++ parameters.path ++ parameters.header,
                                              ordering,
                                              parameters.body,
                                              references)
                .oasEndpointEmitters()

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
            // Annotations collected from the "responses" element that has no direct representation in any model element
            // They will be passed to the ResponsesEmitter
            val orphanAnnotations = operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
            fs.entry(OperationModel.Responses)
              .fold(result += EntryPartEmitter("responses", EmptyMapEmitter()))(f =>
                result += ResponsesEmitter("responses", f, ordering, references, orphanAnnotations))

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

      val parameters = request.queryParameters ++ request.headers
      val payloads   = OasPayloads(request.payloads, endpointPayloadEmitted)

      if (parameters.nonEmpty || payloads.default.isDefined)
        result ++= OasParametersEmitter("parameters", parameters, ordering, payloads.default, references).emitters()

      if (payloads.other.nonEmpty)
        result += OasPayloadsEmitter("x-request-payloads", payloads.other, ordering, references)

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

      fs.entry(RequestModel.UriParameters)
        .map { f =>
          if (f.array.values.nonEmpty)
            result += RamlParametersEmitter("x-baseUriParameters", f, ordering, references)(toRaml(spec))
        }

      result ++= AnnotationsEmitter(request, ordering).emitters

      result
    }
  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit], orphanAnnotations: Seq[DomainExtension])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val emitters = responses(f, ordering) ++ responsesElementsAnnotations()
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.obj(traverse(emitters, _))
        )
      )
    }

    private def responses(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      ordering.sorted(f.array.values.map(e => OasResponseEmitter(e.asInstanceOf[Response], ordering, references)))
    }

    private def responsesElementsAnnotations(): Seq[EntryEmitter] = {
      OrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
    }


    override def position(): Position = pos(f.value.annotations)
  }

  case class EndpointsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit], orphanAnnotations: Seq[DomainExtension])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val emitters = endpoints(f, ordering, references) ++ pathsElementAnnotations()
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.obj(traverse(emitters, _))
        )
      )
    }

    private def pathsElementAnnotations(): Seq[EntryEmitter] = {
      OrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
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
}

class OasSpecEmitter(implicit val spec: OasSpecEmitterContext) extends BaseSpecEmitter {

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

      if (declarations.responses.nonEmpty)
        result += OasDeclaredResponsesEmitter("responses", declarations.responses.values.toSeq, ordering, references)
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
          if (parameter.isLink) OasTagToReferenceEmitter(parameter, parameter.linkLabel, Nil).emit(b)
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
          if (annotationType.isLink) OasTagToReferenceEmitter(annotationType, annotationType.linkLabel, Nil).emit(b)
          else
            spec.factory.annotationTypeEmitter(annotationType, ordering).emitters() match {
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
}

case class OasDeclaredResponsesEmitter(key: String,
                                       responses: Seq[Response],
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj(traverse(ordering.sorted(responses.map(OasResponseEmitter(_, ordering, references: Seq[BaseUnit]))), _)))
  }

  override def position(): Position = responses.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
}
