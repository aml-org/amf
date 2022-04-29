package amf.apicontract.internal.spec.raml.emitter.domain

import amf.apicontract.client.scala.model.domain.Response
import amf.apicontract.internal.metamodel.domain.{RequestModel, ResponseModel}
import amf.apicontract.internal.spec.common.emitter.{AgnosticShapeEmitterContextAdapter, RamlParametersEmitter}
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.{FieldEntry, Fields}
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.common.emitter.{OasResponseExamplesEmitter, ShapeEmitterContext}
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** */
case class RamlResponsesEmitter(
    key: String,
    f: FieldEntry,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    defaultResponse: Boolean = false
)(implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    if (effectiveResponses.nonEmpty) {
      sourceOr(
        f.value.annotations,
        b.entry(key, _.obj { traverse(responses(f, ordering, references), _) })
      )
    }
  }

  private def responses(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val result =
      effectiveResponses.map(e => spec.factory.responseEmitter(e.asInstanceOf[Response], ordering, references))
    ordering.sorted(result)
  }

  override def position(): Position = pos(f.value.annotations)

  protected def effectiveResponses =
    if (defaultResponse) {
      f.array.values.filter(_.asInstanceOf[Response].statusCode.option().getOrElse("default") == "default")
    } else {
      f.array.values.filter(_.asInstanceOf[Response].statusCode.option().getOrElse("default") != "default")
    }
}

case class Raml10ResponsePartEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlResponsePartEmitter(response, ordering, references) {

  override protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    fs.entry(ResponseModel.Examples)
      .map(f => result += OasResponseExamplesEmitter("examples".asRamlAnnotation, f, ordering))

    result ++= AnnotationsEmitter(response, ordering).emitters

    super.emitters(fs) ++ result
  }

}

case class Raml08ResponsePartEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlResponsePartEmitter(response, ordering, references) {}

case class Raml10ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlResponseEmitter(response, ordering, references) {
  override def partBuilder: RamlResponsePartEmitter = Raml10ResponsePartEmitter(response, ordering, references)
}

case class Raml08ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlResponseEmitter(response, ordering, references) {
  override def partBuilder: RamlResponsePartEmitter = Raml08ResponsePartEmitter(response, ordering, references)
}

abstract class RamlResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(ResponseModel.Description).map(f => result += RamlScalarEmitter("description", f))
    fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering, references))
    fs.entry(RequestModel.Payloads).map(f => result += spec.factory.payloadsEmitter("body", f, ordering, references))
    result
  }

  def partBuilder: RamlResponsePartEmitter

  override def emit(b: EntryBuilder): Unit = {
    val fs = response.fields
    sourceOr(
      response.annotations,
      b.complexEntry(
        ScalarEmitter(fs.entry(ResponseModel.StatusCode).map(_.scalar).getOrElse(AmfScalar("default"))).emit(_),
        p => {
          partBuilder.emit(p)
        }
      )
    )
  }

  override def position(): Position = pos(response.annotations)
}

abstract class RamlResponsePartEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(ResponseModel.Description).map(f => result += RamlScalarEmitter("description", f))
    fs.entry(RequestModel.Headers).map(f => result += RamlParametersEmitter("headers", f, ordering, references))
    fs.entry(RequestModel.Payloads).map(f => result += spec.factory.payloadsEmitter("body", f, ordering, references))
    result
  }

  def emit(b: PartBuilder): Unit = {
    val fs = response.fields
    sourceOr(
      response.annotations, {
        if (response.isLink) {
          spec.localReference(response).emit(b)
        } else {
          b.obj { p =>
            traverse(ordering.sorted(emitters(fs)), p)
          }
        }
      }
    )
  }

  override def position(): Position = pos(response.annotations)
}
