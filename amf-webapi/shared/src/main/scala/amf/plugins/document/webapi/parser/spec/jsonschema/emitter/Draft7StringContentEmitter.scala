package amf.plugins.document.webapi.parser.spec.jsonschema.emitter

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.EntryEmitter
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.ScalarShape

import scala.collection.mutable.ArrayBuffer

case class Draft7StringContentEmitter(scalar: ScalarShape){
  def emitters(): Seq[EntryEmitter] = {
    val fs = scalar.fields
    val result: ArrayBuffer[EntryEmitter] = ArrayBuffer()
    fs.entry(ScalarShapeModel.Encoding).foreach(f =>  result += ValueEmitter("contentEncoding", f))
    fs.entry(ScalarShapeModel.MediaType).foreach(f =>  result += ValueEmitter("contentMediaType", f))
    result
  }
}
