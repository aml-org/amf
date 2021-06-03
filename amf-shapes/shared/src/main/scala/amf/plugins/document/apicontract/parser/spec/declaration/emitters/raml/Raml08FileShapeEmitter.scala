package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.DataType
import amf.core.parser.Position
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
