package amf.shapes.internal.spec.raml.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.DataType
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.client.scala.model.domain.{FileShape, ScalarShape}
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import org.yaml.model.YDocument

case class Raml08FileShapeEmitter(shape: FileShape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val scalar = ScalarShape(shape.fields, shape.annotations).withDataType(DataType.File)
    SimpleTypeEmitter(scalar, ordering).emitters().foreach(_.emit(b))
  }

  override def position(): Position = pos(shape.annotations)
}
