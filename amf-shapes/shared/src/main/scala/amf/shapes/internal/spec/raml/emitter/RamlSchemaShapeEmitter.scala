package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, raw, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.domain.metamodel.SchemaShapeModel
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

import scala.collection.mutable

case class RamlSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    if (shape.examples.nonEmpty) {
      val fs     = shape.fields
      val result = mutable.ListBuffer[EntryEmitter]()
      result ++= RamlAnyShapeEmitter(shape, ordering, references).emitters()
      fs.entry(SchemaShapeModel.Raw).foreach { f =>
        result += ValueEmitter("type", f)
      }
      b.obj(traverse(ordering.sorted(result), _))
    } else {
      shape.raw.option() match {
        case Some(r) => raw(b, r)
        case None    => b += YNode.Null
      }
    }
  }

  override def position(): Position = pos(shape.annotations)
}
