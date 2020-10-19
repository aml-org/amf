package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.BaseUnit
import amf.core.parser.{Annotations, FieldEntry, Fields, Position}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.common.ExternalReferenceUrlEmitter.handleInlinedRefOr
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml10TypePartEmitter, RamlNamedTypeEmitter}
import amf.plugins.document.webapi.parser.spec.oas.OasDocumentEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.StringArrayTagsEmitter
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork}
import amf.plugins.domain.webapi.metamodel.{OperationModel, RequestModel}
import amf.plugins.domain.webapi.models.{Callback, Operation, Tag}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class Raml10OperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationEmitter(operation, ordering, references) {
  override protected val partEmitter: RamlOperationPartEmitter =
    Raml10OperationPartEmitter(operation, ordering, references)
}

case class Raml08OperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationEmitter(operation, ordering, references) {
  override protected val partEmitter: RamlOperationPartEmitter =
    Raml08OperationPartEmitter(operation, ordering, references)
}

abstract class RamlOperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  protected val partEmitter: RamlOperationPartEmitter

  override def emit(b: EntryBuilder): Unit = {
    val fs = operation.fields
    sourceOr(
      operation.annotations,
      b.complexEntry(
        ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
        partEmitter.emit
      )
    )
  }

  override def position(): Position = pos(operation.annotations)
}

case class Raml08OperationPartEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationPartEmitter(operation, ordering, references) {
  override protected val baseUriParameterKey: String = "baseUriParameters"
}

case class Raml10OperationPartEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationPartEmitter(operation, ordering, references) {
  override protected val baseUriParameterKey: String = "baseUriParameters".asRamlAnnotation

  override protected def entries(fs: Fields): Seq[EntryEmitter] = {
    val emitters = super.entries(fs)
    val results  = ListBuffer[EntryEmitter]()

    Option(operation.request).foreach { req =>
      req.fields
        .entry(RequestModel.QueryString)
        .foreach { f =>
          Option(f.value.value) match {
            case Some(shape: AnyShape) =>
              results += RamlNamedTypeEmitter(shape, ordering, references, Raml10TypePartEmitter.apply)
            case Some(other) =>
              spec.eh.violation(ResolutionValidation,
                                req.id,
                                None,
                                "Cannot emit non WebApi Shape",
                                other.position(),
                                other.location())
            case _ => // ignore
          }

        }
    }

    results ++ emitters ++ AnnotationsEmitter(operation, ordering).emitters

  }

}

abstract class RamlOperationPartEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {

  protected val baseUriParameterKey: String

  override def emit(b: PartBuilder): Unit = {
    val fs = operation.fields
    b.obj { traverse(ordering.sorted(entries(fs)), _) }
  }

  protected def entries(fs: Fields): Seq[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(OperationModel.Name).map(f => result += ValueEmitter("displayName", f))

    fs.entry(OperationModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("oasDeprecated".asRamlAnnotation, f))

    fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary".asRamlAnnotation, f))

    fs.entry(OperationModel.Tags)
      .map(f =>
        result += StringArrayTagsEmitter("tags".asRamlAnnotation, f.array.values.asInstanceOf[Seq[Tag]], ordering))

    fs.entry(OperationModel.Documentation)
      .map(
        f =>
          result += OasEntryCreativeWorkEmitter("externalDocs".asRamlAnnotation,
                                                f.value.value.asInstanceOf[CreativeWork],
                                                ordering))

    fs.entry(OperationModel.Schemes).map(f => result += spec.arrayEmitter("protocols", f, ordering))

    fs.entry(OperationModel.Accepts).map(f => result += spec.arrayEmitter("consumes".asRamlAnnotation, f, ordering))

    fs.entry(OperationModel.ContentType).map(f => result += ArrayEmitter("produces".asRamlAnnotation, f, ordering))

    fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter(f, ordering)(spec.eh).emitters())

    Option(operation.request).foreach { req =>
      val fields = req.fields

      fields
        .entry(RequestModel.QueryParameters)
        .map(f => result += RamlParametersEmitter("queryParameters", f, ordering, references))

      fields
        .entry(RequestModel.Headers)
        .map(f => result += RamlParametersEmitter("headers", f, ordering, references))
      fields
        .entry(RequestModel.Payloads)
        .map(f => result += spec.factory.payloadsEmitter("body", f, ordering, references))

      fields
        .entry(RequestModel.UriParameters)
        .map { f =>
          if (f.array.values.exists(f => !f.annotations.contains(classOf[SynthesizedField]))) {
            result += RamlParametersEmitter(baseUriParameterKey, f, ordering, references)
          }
        }
    }

    fs.entry(OperationModel.Responses)
      .map(f => result += RamlResponsesEmitter("responses", f, ordering, references))

    fs.entry(OperationModel.Responses)
      .map(
        f =>
          result += RamlResponsesEmitter("defaultResponse".asRamlAnnotation,
                                         f,
                                         ordering,
                                         references,
                                         defaultResponse = true))

    fs.entry(OperationModel.Security)
      .map(f => result += SecurityRequirementsEmitter("securedBy", f, ordering))

    operation.fields.fields().find(_.field == OperationModel.Callbacks) foreach { f: FieldEntry =>
      val callbacks: Seq[Callback] = f.arrayValues
      val annotations              = f.value.annotations
      result += EntryPartEmitter("callbacks".asRamlAnnotation,
                                 OasCallbacksEmitter(callbacks, ordering, references, annotations)(toOas(spec)))

    }
    result
  }

  override def position(): Position = pos(operation.annotations)
}

case class OasCallbacksEmitter(callbacks: Seq[Callback],
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               annotations: Annotations)(implicit spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      annotations,
      b.obj { b =>
        // TODO multiple callbacks may have the same name due to inconsistency in the model, pending refactor in APIMF-1771
        val stringToCallbacks: Map[String, Seq[Callback]] = callbacks.groupBy(_.name.value())
        val emitters = stringToCallbacks.map {
          case (name, callbacks) =>
            EntryPartEmitter(name,
                             OasCallbackEmitter(callbacks, ordering, references),
                             YType.Str,
                             pos(callbacks.headOption.map(_.annotations).getOrElse(Annotations())))
        }.toSeq
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(annotations)
}

case class OasCallbackEmitter(callbacks: Seq[Callback], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {

  override def emit(p: PartBuilder): Unit =
    callbacks.headOption foreach { firstCallback =>
      handleInlinedRefOr(p, firstCallback) {
        if (firstCallback.isLink)
          OasTagToReferenceEmitter(callbacks.head).emit(p)
        else
          p.obj(
            traverse(callbacks.map { callback =>
              OasDocumentEmitter.endpointEmitterWithPath(callback.endpoint,
                                                         callback.expression.value(),
                                                         ordering,
                                                         references,
                                                         spec)
            }, _)
          )
      }
    }

  override def position(): Position = pos(callbacks.headOption.map(_.annotations).getOrElse(Annotations()))

}
