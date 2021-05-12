package amf.plugins.document.webapi.parser.spec.declaration.emitters.schema.json

import amf.core.annotations.{DeclaredElement, ResolvedInheritance}
import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{Emitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.OasShapeDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypeEmitter
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
      spec.forceEmission = None
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
