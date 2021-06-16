package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.{Callback, Operation, Request, Tag}
import amf.apicontract.internal.annotations.FormBodyParameter
import amf.apicontract.internal.metamodel.domain.{OperationModel, RequestModel}
import amf.apicontract.internal.spec.common.emitter.{
  OasCallbacksEmitter,
  OasParametersEmitter,
  OasWithExtensionsSecurityRequirementsEmitter
}
import amf.apicontract.internal.spec.oas.emitter.context.{Oas3SpecEmitterFactory, OasSpecEmitterContext}
import amf.apicontract.internal.spec.oas.emitter.document.Oas3RequestBodyEmitter
import amf.apicontract.internal.spec.raml.emitter
import amf.apicontract.internal.spec.raml.emitter.context.Raml10SpecEmitterContext
import amf.apicontract.internal.spec.raml.emitter.domain.ExtendsEmitter
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils._
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.shapes.client.scala.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.PartBuilder

import scala.collection.mutable

class OperationEmitter(operation: Operation,
                       ordering: SpecOrdering,
                       endpointPayloadEmitted: Boolean,
                       references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends OasLikeOperationEmitter(operation, ordering) {

  override def operationPartEmitter(): PartEmitter =
    OasOperationPartEmitter(operation, ordering, endpointPayloadEmitted, references)
}

case class OasOperationPartEmitter(operation: Operation,
                                   ordering: SpecOrdering,
                                   endpointPayloadEmitted: Boolean,
                                   references: Seq[BaseUnit])(override implicit val spec: OasSpecEmitterContext)
    extends OasLikeOperationPartEmitter(operation, ordering) {
  override def emit(p: PartBuilder): Unit = {
    p.obj { eb =>
      val fs     = operation.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(OperationModel.Tags)
        .map(f => result += StringArrayTagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))
      fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("deprecated", f))
      fs.entry(OperationModel.Schemes).map(f => result += spec.arrayEmitter("schemes", f, ordering))
      fs.entry(OperationModel.Accepts).map(f => result += spec.arrayEmitter("consumes", f, ordering))
      fs.entry(OperationModel.ContentType).map(f => result += spec.arrayEmitter("produces", f, ordering))
      fs.entry(DomainElementModel.Extends)
        .map(f => result ++= ExtendsEmitter(f, ordering, oasExtension = true)(spec.eh).emitters())
      Option(operation.request).foreach(req => result ++= requestEmitters(req, ordering, references))
      // Annotations collected from the "responses" element that has no direct representation in any model element
      // They will be passed to the ResponsesEmitter
      val orphanAnnotations =
        operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
      fs.entry(OperationModel.Responses)
        .fold(result += EntryPartEmitter("responses", EmptyMapEmitter()))(f =>
          result += new ResponsesEmitter("responses", f, ordering, references, orphanAnnotations))

      fs.entry(OperationModel.Security)
        .map(f => result += OasWithExtensionsSecurityRequirementsEmitter("security", f, ordering))

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
      traverse(ordering.sorted(super.commonEmitters ++ result), eb)
    }
  }

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
    val ramlCtx = new Raml10SpecEmitterContext(spec.eh)
    Raml10TypePartEmitter(s, o, a, fs, us)(emitter.RamlShapeEmitterContextAdapter(ramlCtx))
  }

}
