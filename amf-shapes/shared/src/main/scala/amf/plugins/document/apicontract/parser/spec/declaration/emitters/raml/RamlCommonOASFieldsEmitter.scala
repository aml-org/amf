package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.{FieldEntry, Fields, Value}
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel

import scala.collection.mutable.ListBuffer

trait RamlCommonOASFieldsEmitter {
  def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter])(implicit spec: ShapeEmitterContext): Unit = {

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
