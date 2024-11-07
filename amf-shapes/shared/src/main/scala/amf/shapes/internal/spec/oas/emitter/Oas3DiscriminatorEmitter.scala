package amf.shapes.internal.spec.oas.emitter

import amf.aml.internal.annotations.DiscriminatorExtension
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.parser.domain.{FieldEntry, Fields}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.{AstAnnotationEmitter, OasAnnotationEmitter}
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class Oas3DiscriminatorEmitter(
    found: FieldEntry,
    fs: Fields,
    ordering: SpecOrdering,
    extensions: Seq[DomainExtension] = Nil
)(implicit ctx: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "discriminator",
      _.obj { b =>
        val result: ListBuffer[EntryEmitter] = ListBuffer()
        fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("propertyName", f))
        fs.entry(NodeShapeModel.DiscriminatorMapping).map(f => result += IriTemplateEmitter("mapping", f, ordering))
        extensions.filter(ex => ex.extension.annotations.contains(classOf[DiscriminatorExtension])).foreach { ex =>
          new AstAnnotationEmitter(ex, ordering, OasAnnotationEmitter.computeName)(ctx).emit(b)
        }
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(found.value.annotations)
}
