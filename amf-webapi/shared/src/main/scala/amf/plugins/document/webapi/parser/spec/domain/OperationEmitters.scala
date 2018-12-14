package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.BaseUnit
import amf.core.parser.{Fields, Position}
import amf.plugins.document.webapi.contexts.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork}
import amf.plugins.domain.webapi.metamodel.{OperationModel, RequestModel}
import amf.plugins.domain.webapi.models.Operation
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.Strings
import amf.plugins.features.validation.ParserSideValidations

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class Raml10OperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationEmitter(operation, ordering, references) {

  override protected def entries(fs: Fields): Seq[EntryEmitter] = {
    val emitters = super.entries(fs)
    val results  = ListBuffer[EntryEmitter]()

    Option(operation.request).foreach { req =>
      req.fields
        .entry(RequestModel.QueryString)
        .map { f =>
          Option(f.value.value) match {
            case Some(shape: AnyShape) =>
              results += RamlNamedTypeEmitter(shape, ordering, references, Raml10TypePartEmitter.apply)
            case Some(other) =>
              spec.eh.violation(ParserSideValidations.EmittionErrorEspecification.id,
                                "Cannot emit non WebApi Shape",
                                other.position(),
                                other.location())
            case _ => // ignore
          }

        }
    }

    results ++ emitters ++ AnnotationsEmitter(operation, ordering).emitters

  }

  override protected val baseUriParameterKey: String = "baseUriParameters".asRamlAnnotation
}

case class Raml08OperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlOperationEmitter(operation, ordering, references) {

  override protected val baseUriParameterKey: String = "baseUriParameters"
}

abstract class RamlOperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  protected val baseUriParameterKey: String

  protected def entries(fs: Fields): Seq[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(OperationModel.Name).map(f => result += ValueEmitter("displayName", f))

    fs.entry(OperationModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("oasDeprecated".asRamlAnnotation, f))

    fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary".asRamlAnnotation, f))

    fs.entry(OperationModel.Tags).map(f => result += ArrayEmitter("tags".asRamlAnnotation, f, ordering))

    fs.entry(OperationModel.Documentation)
      .map(
        f =>
          result += OasEntryCreativeWorkEmitter("externalDocs".asRamlAnnotation,
                                                f.value.value.asInstanceOf[CreativeWork],
                                                ordering))

    fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("protocols", f, ordering))

    fs.entry(OperationModel.Accepts).map(f => result += ArrayEmitter("consumes".asRamlAnnotation, f, ordering))

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
      .map(f => result += RamlResponsesEmitter("responses", f, ordering, references, defaultResponse = false))

    fs.entry(OperationModel.Responses)
      .map(f => result += RamlResponsesEmitter("defaultResponse".asRamlAnnotation, f, ordering, references, defaultResponse = true))

    fs.entry(OperationModel.Security)
      .map(f => result += ParametrizedSecuritiesSchemeEmitter("securedBy", f, ordering))

    result
  }

  override def emit(b: EntryBuilder): Unit = {
    val fs = operation.fields
    sourceOr(
      operation.annotations,
      b.complexEntry(
        ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
        _.obj { b =>
          traverse(ordering.sorted(entries(fs)), b)
        }
      )
    )
  }

  override def position(): Position = pos(operation.annotations)
}
