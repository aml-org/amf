package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, FacetsEmitter}
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.SchemaShape
import amf.core.utils.AmfStrings

import scala.collection.mutable.ListBuffer

case class OasSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("object", shape)

    fs.entry(SchemaShapeModel.MediaType).map(f => result += ValueEmitter("mediaType".asOasExtension, f))

    fs.entry(SchemaShapeModel.Raw).map(f => result += ValueEmitter("schema".asOasExtension, f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}
