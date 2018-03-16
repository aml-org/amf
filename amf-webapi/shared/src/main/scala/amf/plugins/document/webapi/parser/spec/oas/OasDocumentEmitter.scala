package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.core.remote.{Oas, Vendor}
import amf.plugins.document.webapi.contexts.{BaseSpecEmitter, OasSpecEmitterContext, SpecEmitterContext}
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
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

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
      .map(f => OasNamedRefEmitter("x-extends", f.scalar.toString, pos = pos(f.value.annotations)))
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
    val declares   = OasDeclarationsEmitter(doc.declares, ordering, references.references).emitters
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

      fs.entry(WebApiModel.Tags)
        .map(f => result += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))

      fs.entry(WebApiModel.Documentations).map(f => result ++= OasUserDocumentationsEmitter(f, ordering).emitters())

      // Annotations collected from the "paths" element that has no direct representation in any model element
      // They will be passed to the EndpointsEmitter
      val orphanAnnotations =
        api.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

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
            fs.entry(OperationModel.Tags).map(f => result += ArrayEmitter("tags", f, ordering))
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
            val orphanAnnotations =
              operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
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

  case class ResponsesEmitter(key: String,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              orphanAnnotations: Seq[DomainExtension])
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

  case class EndpointsEmitter(key: String,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              orphanAnnotations: Seq[DomainExtension])
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

}

case class TagsEmitter(key: String, tags: Seq[Tag], ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = tags.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)

  override def emit(b: EntryBuilder): Unit = {
    val emitters = tags.map(t => TagEmitter(t, ordering))
    b.entry(
      key,
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }

  private case class TagEmitter(tag: Tag, ordering: SpecOrdering) extends PartEmitter {

    override def position(): Position = pos(tag.annotations)

    override def emit(p: PartBuilder): Unit = {
      p.obj { b =>
        val fs     = tag.fields
        val result = mutable.ListBuffer[EntryEmitter]()

        fs.entry(TagModel.Name) getOrElse (throw new Exception(s"Cannot declare shape without name $tag"))

        fs.entry(TagModel.Name).map(f => result += ValueEmitter("name", f))
        fs.entry(TagModel.Description).map(f => result += ValueEmitter("description", f))
        fs.entry(TagModel.Documentation)
          .map(f =>
            result +=
              OasEntryCreativeWorkEmitter("externalDocs", tag.documentation, ordering))

        result ++= AnnotationsEmitter(tag, ordering).emitters

        traverse(ordering.sorted(result), b)
      }
    }
  }
}
