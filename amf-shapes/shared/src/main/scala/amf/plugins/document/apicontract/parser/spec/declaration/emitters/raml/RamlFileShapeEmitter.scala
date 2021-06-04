package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, ValueEmitter}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, FileShapeModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models.FileShape

import scala.collection.mutable.ListBuffer

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
