package amf.shapes.internal.spec.oas.emitter

import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.domain.models.SchemaShape
import amf.shapes.internal.domain.metamodel.SchemaShapeModel
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationsEmitter, FacetsEmitter}

import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._

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
