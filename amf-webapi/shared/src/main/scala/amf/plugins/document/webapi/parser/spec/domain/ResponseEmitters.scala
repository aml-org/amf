package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.{EntryEmitter, SpecEmitterContext, SpecOrdering}
import amf.core.emitter.BaseEmitters._
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.domain.webapi.metamodel.{RequestModel, ResponseModel}
import amf.plugins.domain.webapi.models.Response
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class Raml10ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlResponsesEmitter(key, f, ordering, references) {
  override protected def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter =
    Raml10ResponseEmitter.apply
}

case class Raml08ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlResponsesEmitter(key, f, ordering, references) {
  override protected def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter =
    Raml08ResponseEmitter.apply
}

abstract class RamlResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  protected def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter

  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value.annotations,
      b.entry(key, _.obj { traverse(responses(f, ordering, references), _) })
    )
  }

  private def responses(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val result = f.array.values.map(e => responseEmitter(e.asInstanceOf[Response], ordering, references))
    ordering.sorted(result)
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml10ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlResponseEmitter(response, ordering, references) {

  override protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    fs.entry(ResponseModel.Examples).map(f => result += OasResponseExamplesEmitter("(examples)", f, ordering))

    result ++= AnnotationsEmitter(response, ordering).emitters

    super.emitters(fs) ++ result
  }

  override protected def parametersEmitter
    : (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter = Raml10ParametersEmitter.apply

  override protected def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter =
    Raml10PayloadsEmitter.apply
}

case class Raml08ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlResponseEmitter(response, ordering, references) {
  override protected def parametersEmitter
    : (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter = Raml08ParametersEmitter.apply

  override protected def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter =
    Raml08PayloadsEmitter.apply
}

abstract class RamlResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  protected def parametersEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter

  protected def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter

  protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))
    fs.entry(RequestModel.Headers).map(f => result += parametersEmitter("headers", f, ordering, references))
    fs.entry(RequestModel.Payloads).map(f => result += payloadsEmitter("body", f, ordering, references))
    result
  }

  override def emit(b: EntryBuilder): Unit = {
    val fs = response.fields
    sourceOr(
      response.annotations,
      b.complexEntry(
        ScalarEmitter(fs.entry(ResponseModel.StatusCode).get.scalar).emit(_),
        _.obj { b =>
          traverse(ordering.sorted(emitters(fs)), b)
        }
      )
    )
  }

  override def position(): Position = pos(response.annotations)
}
