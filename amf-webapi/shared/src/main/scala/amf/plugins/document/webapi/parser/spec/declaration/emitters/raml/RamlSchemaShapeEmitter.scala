package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, raw, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.SchemaShape
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

import scala.collection.mutable

case class RamlSchemaShapeEmitter(shape: SchemaShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {
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
