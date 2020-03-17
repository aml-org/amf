package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.annotations.SynthesizedField
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position, Value}
import org.yaml.model.{YDocument, YNode}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.model.domain.Shape
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.emitters.AsyncSchemaEmitter
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.YDocument.PartBuilder

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
    val fs        = parameter.fields
    b.entry(
      YNode(paramName),
      partBuilder =>
        if (parameter.isLink) {
          emitLink(partBuilder)
        } else {
          partBuilder.obj {
            emitter =>
              val result = ListBuffer[EntryEmitter]()
              fs.entry(ParameterModel.Description).foreach(f => result += ValueEmitter("description", f))
              fs.entry(ParameterModel.Schema)
                .foreach(f => result += AsyncSchemaEmitter("schema", f.element.asInstanceOf[Shape], ordering, Seq())) // TODO: add references
              fs.entry(ParameterModel.Binding).foreach(f => emitLocation(f, result))
              result ++= AnnotationsEmitter(parameter, ordering).emitters
              traverse(ordering.sorted(result), emitter)
          }
      }
    )
  }

  def emitLink(b: PartBuilder): Unit = {
    val label = OasDefinitions.appendOas3ComponentsPrefix(parameter.linkLabel.value(), "parameters")
    spec.ref(b, label)
  }

  private def emitLocation(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit =
    if (!isInferred(f.value)) result += ValueEmitter("location", f)

  private def isInferred(value: Value) = {
    value.annotations.contains(classOf[SynthesizedField])
  }

  override def position(): Position = pos(parameter.annotations)
}
