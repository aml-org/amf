package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.{FieldEntry, Fields, Value}
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter])(implicit spec: ShapeEmitterContext): Unit = {

    fs.entry(ScalarShapeModel.MinLength).map(f => result += RamlScalarEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += RamlScalarEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum)
      .map(f => result += ValueEmitter("exclusiveMinimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum)
      .map(f => result += ValueEmitter("exclusiveMaximum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.ExclusiveMinimumNumeric)
      .map(f => result += ValueEmitter("exclusiveMinimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.ExclusiveMaximumNumeric)
      .map(f => result += ValueEmitter("exclusiveMaximum".asRamlAnnotation, f))
  }

  def processRamlPattern(f: FieldEntry): FieldEntry = {
    var rawRegex = f.value.value.asInstanceOf[AmfScalar].value.asInstanceOf[String]
    if (rawRegex.startsWith("^")) rawRegex = rawRegex.drop(1)
    if (rawRegex.endsWith("$")) rawRegex = rawRegex.dropRight(1)
    f.copy(value = Value(AmfScalar(rawRegex), f.value.annotations))
  }
}
