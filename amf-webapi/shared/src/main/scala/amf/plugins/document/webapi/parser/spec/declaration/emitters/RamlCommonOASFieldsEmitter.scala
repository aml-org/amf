package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.EntryEmitter
import amf.core.model.domain.AmfScalar
import amf.core.parser.{FieldEntry, Fields, Value}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.core.utils.AmfStrings

import scala.collection.mutable.ListBuffer

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter])(implicit spec: SpecEmitterContext): Unit = {
    fs.entry(ScalarShapeModel.MinLength).map(f => result += RamlScalarEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += RamlScalarEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.ExclusiveMinimum)
      .map(f => result += ValueEmitter("exclusiveMinimum".asRamlAnnotation, f))

    fs.entry(ScalarShapeModel.ExclusiveMaximum)
      .map(f => result += ValueEmitter("exclusiveMaximum".asRamlAnnotation, f))
  }

  def processRamlPattern(f: FieldEntry): FieldEntry = {
    var rawRegex = f.value.value.asInstanceOf[AmfScalar].value.asInstanceOf[String]
    if (rawRegex.startsWith("^")) rawRegex = rawRegex.drop(1)
    if (rawRegex.endsWith("$")) rawRegex = rawRegex.dropRight(1)
    f.copy(value = Value(AmfScalar(rawRegex), f.value.annotations))
  }
}
