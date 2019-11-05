package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.core.remote.{Oas, Vendor}
import amf.core.utils.{IdCounter, Strings}
import amf.plugins.document.webapi.annotations.FormBodyParameter
import amf.plugins.document.webapi.contexts.{
  BaseSpecEmitter,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext,
  Raml10SpecEmitterContext,
  SpecEmitterContext
}
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models._
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait AccessibleOasDocumentEmitters {

  case class EndPointEmitter(endpoint: EndPoint, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit val spec: OasSpecEmitterContext)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = endpoint.fields
      sourceOr(
        endpoint.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit(_),
          _.obj { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName".asOasExtension, f))
            fs.entry(EndPointModel.Description).map { f =>
              val descriptionKey =
                if (spec.isInstanceOf[Oas3SpecEmitterContext]) "description" else "description".asOasExtension
              result += ValueEmitter(descriptionKey, f)
            }
            fs.entry(DomainElementModel.Extends)
              .map(f => result ++= ExtendsEmitter(f, ordering, oasExtension = true)(spec.eh).emitters())

            val parameters =
              Parameters.classified(endpoint.path.value(), endpoint.parameters, endpoint.payloads)

            spec match {
              case _: Oas3SpecEmitterContext =>
                fs.entry(EndPointModel.Summary).map(f => result += ValueEmitter("summary", f))

                fs.entry(EndPointModel.Servers).map { f =>
                  result ++= spec.factory
                    .asInstanceOf[Oas3SpecEmitterFactory]
                    .serversEmitter(endpoint, f, ordering, references)
                    .emitters()
                }

              case _ => //
            }

            if (parameters.nonEmpty)
              result ++= OasParametersEmitter(
                "parameters",
                parameters.query ++ parameters.path ++ parameters.header ++ parameters.cookie,
                ordering,
                parameters.body,
                references)
                .oasEndpointEmitters()

            fs.entry(EndPointModel.Operations)
              .map(f => result ++= operations(f, ordering, parameters.body.nonEmpty, references))

            fs.entry(EndPointModel.Security)
              .map(f => result += ParametrizedSecuritiesSchemeEmitter("security".asOasExtension, f, ordering))

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
                              references: Seq[BaseUnit])(implicit val spec: OasSpecEmitterContext)
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
            fs.entry(DomainElementModel.Extends)
              .map(f => result ++= ExtendsEmitter(f, ordering, oasExtension = true)(spec.eh).emitters())
            Option(operation.request).foreach(req => result ++= requestEmitters(req, ordering, references))
            // Annotations collected from the "responses" element that has no direct representation in any model element
            // They will be passed to the ResponsesEmitter
            val orphanAnnotations =
              operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
            fs.entry(OperationModel.Responses)
              .fold(result += EntryPartEmitter("responses", EmptyMapEmitter()))(f =>
                result += ResponsesEmitter("responses", f, ordering, references, orphanAnnotations))

            fs.entry(OperationModel.Security)
              .map(f => result += ParametrizedSecuritiesSchemeEmitter("security", f, ordering))

            if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {
              operation.fields.fields().find(_.field == OperationModel.Callbacks) foreach { f: FieldEntry =>
                val callbacks: Seq[Callback] = f.arrayValues
                val annotations              = f.value.annotations
                result += EntryPartEmitter("callbacks",
                                           OasCallbacksEmitter(callbacks, ordering, references, annotations)(spec))
              }

              fs.entry(OperationModel.Servers)
                .map(f => result ++= spec.factory.serversEmitter(operation, f, ordering, references).emitters())

            }
            result ++= AnnotationsEmitter(operation, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(operation.annotations)

    def requestEmitters(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {

      val result = mutable.ListBuffer[EntryEmitter]()

      if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {

        // OAS 3.0.0
        val parameters = request.queryParameters ++ request.uriParameters ++ request.headers ++ request.cookieParameters
        if (parameters.nonEmpty)
          result ++= OasParametersEmitter("parameters", parameters, ordering, Nil, references).emitters()
        result ++= Seq(Oas3RequestBodyEmitter(request, ordering, references))

      } else {

        // OAS 2.0
        val fs               = request.fields
        val parameters       = request.queryParameters ++ request.uriParameters ++ request.headers
        val (body, formData) = request.payloads.partition(p => !p.annotations.contains(classOf[FormBodyParameter]))

        val payloads = OasPayloads(body)

        if (parameters.nonEmpty || payloads.default.isDefined || formData.nonEmpty)
          result ++= OasParametersEmitter("parameters",
                                          parameters,
                                          ordering,
                                          payloads.default.toSeq ++ formData,
                                          references)
            .emitters()

        if (payloads.other.nonEmpty)
          result += OasPayloadsEmitter("requestPayloads".asOasExtension, payloads.other, ordering, references)

        fs.entry(RequestModel.QueryString)
          .foreach { f =>
            Option(f.value.value) match {
              case Some(shape: AnyShape) =>
                result += RamlNamedTypeEmitter(shape, ordering, Nil, ramlTypesEmitter)
              case Some(other) =>
                spec.eh.violation(ResolutionValidation,
                                  request.id,
                                  None,
                                  "Cannot emit a non WebApi Shape",
                                  other.position(),
                                  other.location())
              case None => // ignore
            }
          }
      }

      result ++= AnnotationsEmitter(request, ordering).emitters

      result
    }

    def ramlTypesEmitter(s: AnyShape,
                         o: SpecOrdering,
                         a: Option[AnnotationsEmitter],
                         fs: Seq[Field],
                         us: Seq[BaseUnit]): RamlTypePartEmitter = {
      Raml10TypePartEmitter(s, o, a, fs, us)(new Raml10SpecEmitterContext(spec.eh))
    }

  }

  case class ResponsesEmitter(key: String,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              orphanAnnotations: Seq[DomainExtension])(implicit val spec: OasSpecEmitterContext)
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
}

/**
  * OpenAPI Spec Emitter.
  */
abstract class OasDocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasSpecEmitter
    with AccessibleOasDocumentEmitters {

  private def retrieveWebApi(): WebApi = document match {
    case document: Document => document.encodes.asInstanceOf[WebApi]
    case _ =>
      spec.eh.violation(ResolutionValidation,
                        document.id,
                        None,
                        "BaseUnit doesn't encode a WebApi.",
                        document.position(),
                        document.location())
      WebApi()
  }

  def extensionEmitter(): Seq[EntryEmitter] =
    document.fields
      .entry(ExtensionLikeModel.Extends)
      .map(f => OasNamedRefEmitter("extends".asOasExtension, f.scalar.toString, pos = pos(f.value.annotations)))
      .toList ++ retrieveHeader()

  private def retrieveHeader() = document match {
    case _: Extension => Some(MapEntryEmitter(Oas20Extension.tuple))
    case _: Overlay   => Some(MapEntryEmitter(Oas20Overlay.tuple))
    case _: Document  => None
    case _            => throw new Exception("Document has no header.")
  }

  protected def wrapDeclarations(emitters: Seq[EntryEmitter], ordering: SpecOrdering): Seq[EntryEmitter] = emitters

  def emitDocument(): YDocument = {
    val doc = document.asInstanceOf[Document]

    val ordering = SpecOrdering.ordering(Oas, doc.encodes.annotations)

    val references = ReferencesEmitter(document, ordering)
    val declares =
      wrapDeclarations(OasDeclarationsEmitter(doc.declares, ordering, document.references).emitters, ordering)
    val api       = emitWebApi(ordering, document.references)
    val extension = extensionEmitter()
    val usage: Option[ValueEmitter] =
      doc.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage".asOasExtension, f))

    YDocument {
      _.obj { b =>
        versionEntry(b)
        traverse(ordering.sorted(api ++ extension ++ usage ++ declares :+ references), b)
      }
    }
  }

  protected def versionEntry(b: EntryBuilder): Unit

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

      fs.entry(WebApiModel.Servers)
        .map(f => result ++= spec.factory.serversEmitter(api, f, ordering, references).emitters())

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

case class Oas3RequestBodyEmitter(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    if (request.isLink) {
      val refUrl = OasDefinitions.appendOas3ComponentsPrefix(request.linkLabel.value(), "requestBodies")
      b.entry("requestBody", _.obj(_.entry("$ref", refUrl)))
    } else {
      val result = Oas3RequestBodyEmitter.emitters(request, ordering, references)
      if (result.nonEmpty)
        b.entry("requestBody", _.obj(traverse(ordering.sorted(result), _)))
    }
  }

  override def position(): Position = pos(request.payloads.headOption.getOrElse(request).annotations)
}

object Oas3RequestBodyEmitter {
  def emitters(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: OasSpecEmitterContext): ListBuffer[EntryEmitter] = {
    val fs     = request.fields
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(RequestModel.Description).map(f => result += ValueEmitter("description", f))
    fs.entry(RequestModel.Required).map(f => result += ValueEmitter("required", f))
    request.fields.fields().find(_.field == RequestModel.Payloads) foreach { f: FieldEntry =>
      val payloads: Seq[Payload] = f.arrayValues
      val annotations            = f.value.annotations
      result += EntryPartEmitter("content", OasContentPayloadsEmitter(payloads, ordering, references, annotations))
    }
    result
  }
}

case class Oas3RequestBodyDeclarationsEmitter(requests: Seq[Request],
                                              ordering: SpecOrdering,
                                              references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "requestBodies",
      _.obj(decBuilder => {
        requests.foreach(request => {
          val result = Oas3RequestBodyEmitter.emitters(request, ordering, references)
          if (result.nonEmpty)
            decBuilder.entry(request.name.value(), _.obj(traverse(ordering.sorted(result), _)))
        })
      })
    )
  }

  override def position(): Position = {
    requests.headOption.map(rq => pos(rq.payloads.headOption.getOrElse(rq).annotations)).getOrElse(Position.ZERO)
  }
}

class OasSpecEmitter(implicit val spec: OasSpecEmitterContext) extends BaseSpecEmitter {

  case class ReferencesEmitter(baseUnit: BaseUnit, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val aliases    = baseUnit.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set()))
      val references = baseUnit.references
      val modules    = references.collect({ case m: Module => m })
      if (modules.nonEmpty) {
        var modulesEmitted = Map[String, Module]()
        val idCounter      = new IdCounter()
        val aliasesEmitters: Seq[Option[EntryEmitter]] = aliases.aliases.map {
          case (alias, (fullUrl, localUrl)) =>
            modules.find(_.id == fullUrl) match {
              case Some(module) =>
                modulesEmitted += (module.id -> module)
                Some(
                  ReferenceEmitter(module,
                                   Some(Aliases(Set(alias -> (fullUrl, localUrl)))),
                                   ordering,
                                   () => idCounter.genId("uses")))
              case _ => None
            }
        }.toSeq
        val missingModuleEmitters = modules.filter(m => modulesEmitted.get(m.id).isEmpty).map { module =>
          Some(ReferenceEmitter(module, Some(Aliases(Set())), ordering, () => idCounter.genId("uses")))
        }
        val finalEmitters = (aliasesEmitters ++ missingModuleEmitters).collect { case Some(e) => e }
        b.entry("uses".asOasExtension, _.obj { b =>
          traverse(ordering.sorted(finalEmitters), b)
        })
      }
    }

    override def position(): Position = ZERO
  }

  case class ReferenceEmitter(reference: BaseUnit,
                              aliases: Option[Aliases],
                              ordering: SpecOrdering,
                              aliasGenerator: () => String)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val aliasesMap = aliases.getOrElse(Aliases(Set())).aliases
      val effectiveAlias = aliasesMap.find { case (_, (f, _)) => f == reference.id } map { case (a, (_, r)) => (a, r) } getOrElse {
        (aliasGenerator(), name)
      }
      MapEntryEmitter(effectiveAlias._1, effectiveAlias._2).emit(b)
    }

    private def name: String = reference.location().getOrElse(reference.id)

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

        fs.entry(TagModel.Name).map(f => result += ValueEmitter("name", f))
        fs.entry(TagModel.Description).map(f => result += ValueEmitter("description", f))
        fs.entry(TagModel.Documentation)
          .map(_ =>
            result +=
              OasEntryCreativeWorkEmitter("externalDocs", tag.documentation, ordering))

        result ++= AnnotationsEmitter(tag, ordering).emitters

        traverse(ordering.sorted(result), b)
      }
    }
  }
}

object OasDocumentEmitter extends AccessibleOasDocumentEmitters {
  def endpointEmitter(endpoint: EndPoint,
                      ordering: SpecOrdering,
                      references: Seq[BaseUnit],
                      spec: OasSpecEmitterContext) = {
    EndPointEmitter(endpoint, ordering, references)(spec)
  }
}
