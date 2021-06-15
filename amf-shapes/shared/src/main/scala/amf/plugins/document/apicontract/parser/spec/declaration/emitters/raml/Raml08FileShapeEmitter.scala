package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.client.scala.model.DataType
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{ShapeEmitterContext, SimpleTypeEmitter}
import amf.plugins.domain.shapes.models.{FileShape, ScalarShape}
import org.yaml.model.YDocument

case class Raml08FileShapeEmitter(shape: FileShape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val scalar = ScalarShape(shape.fields, shape.annotations).withDataType(DataType.File)
    SimpleTypeEmitter(scalar, ordering).emitters().foreach(_.emit(b))
  }

  override def position(): Position = pos(shape.annotations)
}
