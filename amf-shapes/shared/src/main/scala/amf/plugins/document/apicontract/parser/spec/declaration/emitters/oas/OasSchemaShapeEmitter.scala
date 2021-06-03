package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{OasTypeFacetEmitter, ShapeEmitterContext}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  FacetsEmitter
}
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.SchemaShape

import scala.collection.mutable.ListBuffer

case class OasSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    result += OasTypeFacetEmitter("object", shape)

    fs.entry(SchemaShapeModel.MediaType).map(f => result += ValueEmitter("mediaType".asOasExtension, f))

    fs.entry(SchemaShapeModel.Raw).map(f => result += ValueEmitter("schema".asOasExtension, f))

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}
