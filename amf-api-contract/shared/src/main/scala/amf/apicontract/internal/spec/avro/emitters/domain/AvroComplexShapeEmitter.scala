package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

abstract class AvroComplexShapeEmitter(
    shape: Shape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val avroType = spec.getAvroType(shape).getOrElse("default")
    avroType match {
      case "union" => // do not emit "type": "union"
      // do not emit type in record fields (it's inside the range)
      case s: String if !shape.isInstanceOf[PropertyShape] => b.entry("type", s)
      case _                                               => // ignore
    }
    emitCommonFields(b)
    emitSpecificFields(b)
    emitDefault(b)
  }

  def emitCommonFields(b: EntryBuilder): Unit = {
    shape.fields.entry(AnyShapeModel.Name).foreach(f => b.entry("name", f.scalar.toString))
    shape.fields.entry(AnyShapeModel.AvroNamespace).foreach(f => b.entry("namespace", f.scalar.toString))
    shape.fields.entry(AnyShapeModel.Aliases).foreach(f => spec.arrayEmitter("aliases", f, ordering).emit(b))
    shape.fields.entry(AnyShapeModel.Description).foreach(f => b.entry("doc", f.scalar.toString))
  }

  def emitSpecificFields(b: EntryBuilder): Unit

  def emitDefault(b: EntryBuilder): Unit = {
    shape match {
      case p: PropertyShape if p.range.fields.entry(ShapeModel.Default).isDefined => return
      case _                                                                      =>
    }
    shape.fields.entry(ShapeModel.Default).foreach { _ =>
      b.entry("default", DataNodeEmitter(shape.default, ordering)(spec.eh).emit(_))
    }
  }

  override def position(): Position = pos(shape.annotations)
}
