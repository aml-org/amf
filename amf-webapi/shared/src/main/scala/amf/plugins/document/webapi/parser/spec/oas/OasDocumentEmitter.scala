package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations._
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document._
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position}
import amf.core.remote.{Oas, Vendor}
import amf.core.utils.{AmfStrings, IdCounter}
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas3SpecEmitterContext,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  OrphanAnnotationsEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.common.ExternalReferenceUrlEmitter.handleInlinedRefOr
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.emitters.{
  InfoEmitter,
  OperationEmitter,
  TagsEmitter,
  UserDocumentationsEmitter
}
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class EndPointEmitter(endpoint: EndPoint,
                           pathName: Option[String] = None,
                           ordering: SpecOrdering,
                           references: Seq[BaseUnit])(implicit val spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = endpoint.fields
    sourceOr(
      endpoint.annotations,
      b.complexEntry(
        ScalarEmitter(pathName.map(AmfScalar(_)).getOrElse(fs.entry(EndPointModel.Path).get.scalar)).emit(_),
        EndPointPartEmitter(endpoint, ordering, references).emit(_)
      )
    )
  }

  override def position(): Position = pos(endpoint.annotations)
}

case class EndPointPartEmitter(endpoint: EndPoint, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit val spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    val fs = endpoint.fields
    b.obj { b =>
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
        result ++= OasParametersEmitter("parameters",
                                        parameters.query ++ parameters.path ++ parameters.header ++ parameters.cookie,
                                        ordering,
                                        parameters.body,
                                        references)
          .oasEndpointEmitters()

      fs.entry(EndPointModel.Operations)
        .map(f => result ++= operations(f, ordering, parameters.body.nonEmpty, references))

      fs.entry(EndPointModel.Security)
        .map(f => result += SecurityRequirementsEmitter("security".asOasExtension, f, ordering))

      result ++= AnnotationsEmitter(endpoint, ordering).emitters

      traverse(ordering.sorted(result), b)
    }

  }

  private def operations(f: FieldEntry,
                         ordering: SpecOrdering,
                         endpointPayloadEmitted: Boolean,
                         references: Seq[BaseUnit]): Seq[EntryEmitter] =
    f.array.values
      .map(e => new OperationEmitter(e.asInstanceOf[Operation], ordering, endpointPayloadEmitted, references))

  override def position(): Position = pos(endpoint.annotations)
}

object EndPointEmitter {
  def apply(endpoint: EndPoint, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: OasSpecEmitterContext): EndPointEmitter =
    new EndPointEmitter(endpoint, None, ordering, references)(spec)
}

/**
  * OpenAPI Spec Emitter.
  */
abstract class OasDocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasSpecEmitter {

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
    def declares: Seq[EntryEmitter] =
      wrapDeclarations(OasDeclarationsEmitter(doc.declares, ordering, document.references).emitters, ordering)
    val api       = emitWebApi(ordering, document.references)
    val extension = extensionEmitter()
    val usage: Option[ValueEmitter] =
      doc.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage".asOasExtension, f))

    YDocument {
      _.obj { b =>
        versionEntry(b)
        traverse(ordering.sorted(api), b) // api explicitly needs to be emitted before declares to populate compact emission queue
        traverse(ordering.sorted(extension ++ usage ++ declares :+ references), b)
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
        .map(f => result += spec.arrayEmitter("consumes", f, ordering))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += spec.arrayEmitter("produces", f, ordering))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += spec.arrayEmitter("schemes", f, ordering))

      fs.entry(WebApiModel.Tags)
        .map(f => result += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))

      fs.entry(WebApiModel.Documentations).map(f => result ++= UserDocumentationsEmitter(f, ordering).emitters())

      // Annotations collected from the "paths" element that has no direct representation in any model element
      // They will be passed to the EndpointsEmitter
      val orphanAnnotations =
        api.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

      fs.entry(WebApiModel.EndPoints)
        .fold(result += EntryPartEmitter("paths", EmptyMapEmitter()))(f =>
          result += EndpointsEmitter("paths", f, ordering, references, orphanAnnotations))

      fs.entry(WebApiModel.Security)
        .map(f => result += OasWithExtensionsSecurityRequirementsEmitter("security", f, ordering))

      result ++= AnnotationsEmitter(api, ordering).emitters

      ordering.sorted(result)
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
}

case class Oas3RequestBodyEmitter(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    if (request.isLink) {
      val refUrl = OasDefinitions.appendOas3ComponentsPrefix(request.linkLabel.value(), "requestBodies")
      b.entry("requestBody", _.obj(_.entry("$ref", refUrl)))
    } else {
      val partEmitter: Oas3RequestBodyPartEmitter = Oas3RequestBodyPartEmitter(request, ordering, references)
      if (partEmitter.emitters.nonEmpty)
        b.entry("requestBody", partEmitter.emit(_))
    }
  }

  override def position(): Position = pos(request.payloads.headOption.getOrElse(request).annotations)
}

case class Oas3RequestBodyPartEmitter(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    handleInlinedRefOr(b, request) {
      if (request.isLink) {
        val refUrl = OasDefinitions.appendOas3ComponentsPrefix(request.linkLabel.value(), "requestBodies")
        b.obj(_.entry("$ref", refUrl))
      } else {
        val result = emitters
        b.obj(traverse(ordering.sorted(result), _))
      }
    }

  val emitters: ListBuffer[EntryEmitter] = {
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

  override def position(): Position = pos(request.payloads.headOption.getOrElse(request).annotations)
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
          val partEmitter: Oas3RequestBodyPartEmitter = Oas3RequestBodyPartEmitter(request, ordering, references)
          if (partEmitter.emitters.nonEmpty)
            decBuilder.entry(request.name.value(), partEmitter.emit(_))
        })
      })
    )
  }

  override def position(): Position = {
    requests.headOption.map(rq => pos(rq.payloads.headOption.getOrElse(rq).annotations)).getOrElse(Position.ZERO)
  }
}

class OasSpecEmitter(implicit val spec: SpecEmitterContext) extends BaseSpecEmitter {

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

object OasDocumentEmitter {
  def endpointEmitter(endpoint: EndPoint,
                      ordering: SpecOrdering,
                      references: Seq[BaseUnit],
                      spec: OasSpecEmitterContext) = {
    EndPointEmitter(endpoint, ordering, references)(spec)
  }

  def endpointEmitterWithPath(endpoint: EndPoint,
                              path: String,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              spec: OasSpecEmitterContext) = {
    endpoint.withPath(path)
    EndPointEmitter(endpoint, Some(path), ordering, references)(spec)
  }
}
