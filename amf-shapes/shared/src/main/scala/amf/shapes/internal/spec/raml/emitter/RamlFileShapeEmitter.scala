package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, ValueEmitter}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, FileShapeModel, ScalarShapeModel}
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext

import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.FileShape

case class RamlFileShapeEmitter(scalar: FileShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlAnyShapeEmitter(scalar, ordering, references)
    with RamlCommonOASFieldsEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = scalar.fields

    emitOASFields(fs, result)

    fs.entry(FileShapeModel.FileTypes).map(f => result += spec.arrayEmitter("fileTypes", f, ordering))

    fs.entry(ScalarShapeModel.Pattern).map { f =>
      result += ValueEmitter("pattern".asRamlAnnotation, processRamlPattern(f))
    }

    fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf".asRamlAnnotation, f))

    if (result.isEmpty || (result.size == 1 && scalar.fields.?(AnyShapeModel.Examples).nonEmpty))
      result += MapEntryEmitter("type", "file")

    result
  }

  override val typeName: Option[String] = Some("file")
}
