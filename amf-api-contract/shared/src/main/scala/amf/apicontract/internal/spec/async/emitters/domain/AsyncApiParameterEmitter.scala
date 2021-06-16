package amf.apicontract.internal.spec.async.emitters.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.{FieldEntry, Value}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val parameterEmitters = parameters.map(p => new AsyncApiSingleParameterEmitter(p, ordering))
    b.entry(
      "parameters",
      _.obj(pb => parameterEmitters.foreach(e => e.emit(pb)))
    )
  }

  override def position(): Position = parameters.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

class AsyncApiSingleParameterEmitter(parameter: Parameter, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val paramName = parameter.name.value()
    b.entry(
      YNode(paramName),
      AsyncApiSingleParameterPartEmitter(parameter, ordering).emit(_)
    )
  }

  override def position(): Position = pos(parameter.annotations)
}

case class AsyncApiSingleParameterPartEmitter(parameter: Parameter, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {
  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: YDocument.PartBuilder): Unit = {
    val fs = parameter.fields
    if (parameter.isLink) {
      emitLink(b)
    } else {
      b.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        fs.entry(ParameterModel.Description).foreach(f => result += ValueEmitter("description", f))
        fs.entry(ParameterModel.Schema)
          .foreach(f => result += AsyncSchemaEmitter("schema", f.element.asInstanceOf[Shape], ordering, Seq()))
        fs.entry(ParameterModel.Binding).foreach(f => emitLocation(f, result))
        result ++= AnnotationsEmitter(parameter, ordering).emitters
        traverse(ordering.sorted(result), emitter)
      }
    }
  }

  def emitLink(b: PartBuilder): Unit = {
    oas.emitter.OasTagToReferenceEmitter(parameter).emit(b)
  }

  private def emitLocation(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit =
    if (!isInferred(f.value)) result += ValueEmitter("location", f)

  private def isInferred(value: Value) = value.annotations.contains(classOf[SynthesizedField])

  override def position(): Position = pos(parameter.annotations)
}
