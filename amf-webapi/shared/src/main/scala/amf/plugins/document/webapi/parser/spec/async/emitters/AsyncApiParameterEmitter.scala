package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.annotations.SynthesizedField
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position, Value}
import amf.plugins.document.webapi.contexts.emitter.async.AsyncSpecEmitterContext
import org.yaml.model.{YDocument, YNode, YScalar}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, AsyncSchemaEmitter}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter

import scala.collection.mutable.ListBuffer

class AsyncApiParametersEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val parameters = f.arrayValues[Parameter].map(p => new AsyncApiSingleParameterEmitter(p, ordering))
    b.entry(
      "parameters",
      _.obj(pb => parameters.foreach(e => e.emit(pb)))
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

class AsyncApiSingleParameterEmitter(parameter: Parameter, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val paramName = parameter.name.value()
    val fs        = parameter.fields
    b.entry(
      YNode(paramName),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        fs.entry(ParameterModel.Description).foreach(f => result += ValueEmitter("description", f))
        fs.entry(ParameterModel.Schema)
          .foreach(f => result += AsyncSchemaEmitter("schema", f.element.asInstanceOf[Shape], ordering, Seq())) // TODO: add references
        fs.entry(ParameterModel.Binding).foreach(f => emitLocation(f, result))
        result ++= AnnotationsEmitter(parameter, ordering).emitters
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  private def emitLocation(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit =
    if (!isInferred(f.value)) result += ValueEmitter("location", f)

  private def isInferred(value: Value) = {
    value.annotations.contains(classOf[SynthesizedField])
  }

  override def position(): Position = pos(parameter.annotations)
}
