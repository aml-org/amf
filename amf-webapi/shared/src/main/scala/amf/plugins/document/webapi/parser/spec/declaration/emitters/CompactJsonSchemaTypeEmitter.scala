package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.annotations.{DeclaredElement, ResolvedInheritance}
import amf.core.emitter.{Emitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.document.webapi.contexts.emitter.oas.CompactJsonSchemaEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.OasTagToReferenceEmitter

case class CompactJsonSchemaTypeEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    ignored: Seq[Field],
    references: Seq[BaseUnit],
    pointer: Seq[String],
    schemaPath: Seq[(String, String)])(implicit spec: CompactJsonSchemaEmitterContext) {
  def emitters(): Seq[Emitter] = {
    val definitionQueue = spec.definitionsQueue
    if (spec.forceEmission.contains(shape.id) || emitInlined()) {
      spec.forceEmission = None
      OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
    } else {
      val label = definitionQueue.enqueue(shape)
      val tag   = OasDefinitions.appendDefinitionsPrefix(label)
      Seq(OasTagToReferenceEmitter(shape, Some(tag), Nil))
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
