package amf.apicontract.internal.spec.oas.emitter.document

import amf.apicontract.client.scala.model.document.{Extension, Overlay}
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.{EndPointModel, RequestModel}
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.emitter.{
  AgnosticShapeEmitterContextAdapter,
  OasParametersEmitter,
  OasWithExtensionsSecurityRequirementsEmitter,
  SecurityRequirementsEmitter
}
import amf.apicontract.internal.spec.oas.OasHeader.{Oas20Extension, Oas20Overlay}
import amf.apicontract.internal.spec.oas.emitter.context.{
  Oas3SpecEmitterContext,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.apicontract.internal.spec.oas.emitter.domain._
import amf.apicontract.internal.spec.raml.emitter.domain.ExtendsEmitter
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.{Oas20, Spec}
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.ExternalReferenceUrlEmitter.handleInlinedRefOr
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.oas.emitter.{OasOrphanAnnotationsEmitter, OasSpecEmitter}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class EndPointEmitter(endpoint: EndPoint,
                           pathName: Option[String] = None,
                           ordering: SpecOrdering,
                           references: Seq[BaseUnit])(implicit val specCtx: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = endpoint.fields
    sourceOr(
      endpoint.annotations,
      b.complexEntry(
        ScalarEmitter(
          pathName.map(AmfScalar(_)).getOrElse(fs.entry(EndPointModel.Path).map(_.scalar).getOrElse(AmfScalar(""))))
          .emit(_),
        EndPointPartEmitter(endpoint, ordering, references).emit(_)
      )
    )
  }

  override def position(): Position = pos(endpoint.annotations)
}

case class EndPointPartEmitter(endpoint: EndPoint, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit val specCtx: OasSpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(specCtx)

  override def emit(b: PartBuilder): Unit = {
    val fs = endpoint.fields
    b.obj { b =>
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName".asOasExtension, f))
      fs.entry(EndPointModel.Description).map { f =>
        val descriptionKey =
          if (specCtx.isInstanceOf[Oas3SpecEmitterContext]) "description" else "description".asOasExtension
        result += ValueEmitter(descriptionKey, f)
      }
      fs.entry(DomainElementModel.Extends)
        .map(f => result ++= ExtendsEmitter(f, ordering, oasExtension = true)(specCtx.eh).emitters())

      val parameters =
        Parameters.classified(endpoint.path.value(), endpoint.parameters, endpoint.payloads)

      specCtx match {
        case _: Oas3SpecEmitterContext =>
          fs.entry(EndPointModel.Summary).map(f => result += ValueEmitter("summary", f))

          fs.entry(EndPointModel.Servers).map { f =>
            result ++= specCtx.factory
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
      implicit specCtx: OasSpecEmitterContext): EndPointEmitter =
    new EndPointEmitter(endpoint, None, ordering, references)(specCtx)
}

/**
  * OpenAPI Spec Emitter.
  */
abstract class OasDocumentEmitter(document: BaseUnit)(implicit val specCtx: OasSpecEmitterContext)
    extends OasSpecEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(specCtx)

  private def retrieveWebApi(): WebApi = document match {
    case document: Document => document.encodes.asInstanceOf[WebApi]
    case _ =>
      specCtx.eh.violation(TransformationValidation,
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

    val ordering = SpecOrdering.ordering(Oas20, doc.sourceSpec)

    val references = ReferencesEmitter(document, ordering)
    val api        = emitWebApi(ordering, document.references)
    def declares: Seq[EntryEmitter] =
      wrapDeclarations(OasDeclarationsEmitter(doc.declares, ordering, document.references).emitters, ordering)
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
    val model = retrieveWebApi()
    val spec  = document.sourceSpec
    val api   = WebApiEmitter(model, ordering, spec, references)
    api.emitters
  }

  case class WebApiEmitter(api: WebApi, ordering: SpecOrdering, spec: Option[Spec], references: Seq[BaseUnit]) {
    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Servers)
        .map(f => result ++= specCtx.factory.serversEmitter(api, f, ordering, references).emitters())

      fs.entry(WebApiModel.Accepts)
        .map(f => result += specCtx.arrayEmitter("consumes", f, ordering))

      fs.entry(WebApiModel.ContentType)
        .map(f => result += specCtx.arrayEmitter("produces", f, ordering))

      fs.entry(WebApiModel.Schemes)
        .map(f => result += specCtx.arrayEmitter("schemes", f, ordering))

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
      OasOrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
      val result = f.array.values.map(e => EndPointEmitter(e.asInstanceOf[EndPoint], ordering, references))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }
}

case class Oas3RequestBodyEmitter(request: Request, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit specCtx: OasSpecEmitterContext)
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
    implicit specCtx: OasSpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(specCtx)

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
                                              references: Seq[BaseUnit])(implicit specCtx: OasSpecEmitterContext)
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

object OasDocumentEmitter {
  def endpointEmitter(endpoint: EndPoint,
                      ordering: SpecOrdering,
                      references: Seq[BaseUnit],
                      specCtx: OasSpecEmitterContext) = {
    EndPointEmitter(endpoint, ordering, references)(specCtx)
  }

  def endpointEmitterWithPath(endpoint: EndPoint,
                              path: String,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              specCtx: OasSpecEmitterContext) = {
    endpoint.withPath(path)
    EndPointEmitter(endpoint, Some(path), ordering, references)(specCtx)
  }
}
