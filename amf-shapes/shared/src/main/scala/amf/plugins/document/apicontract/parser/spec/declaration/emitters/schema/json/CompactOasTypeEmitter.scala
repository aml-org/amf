package amf.plugins.document.apicontract.parser.spec.declaration.emitters.schema.json

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.internal.annotations.{DeclaredElement, ResolvedInheritance}
import amf.core.internal.metamodel.Field
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, PartEmitter}
import amf.plugins.document.apicontract.parser.spec.OasShapeDefinitions
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.OasTypeEmitter
import org.yaml.model.YDocument

case class CompactOasTypeEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 ignored: Seq[Field],
                                 references: Seq[BaseUnit],
                                 pointer: Seq[String],
                                 schemaPath: Seq[(String, String)])(implicit spec: OasLikeShapeEmitterContext) {

  def emitters(): Seq[Emitter] = {
    val definitionQueue = spec.definitionsQueue
    if (spec.forceEmission.contains(shape.id) || emitInlined()) {
      spec.removeForceEmission
      OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
    } else {
      val label = definitionQueue.enqueue(shape)
      val tag   = OasShapeDefinitions.appendSchemasPrefix(label, Some(spec.vendor))
      Seq(refEmitter(tag))
    }
  }

  private def refEmitter(tag: String) = {
    new PartEmitter {
      override def emit(b: YDocument.PartBuilder): Unit = spec.ref(b, tag)
      override def position(): Position                 = pos(shape.annotations)
    }
  }

  def emitInlined(): Boolean =
    shape.annotations
      .find(a =>
        a match {
          case _: DeclaredElement     => true
          case _: ResolvedInheritance => true
          case _                      => false
      })
      .isEmpty || shape.isInstanceOf[RecursiveShape]
}
