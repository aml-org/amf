package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.{OasSpecEmitterContext, RamlSpecEmitterContext, SpecEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder

case class RamlDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: RamlSpecEmitterContext)
    extends DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering) {
  override def emitTypes(b: EntryBuilder): Unit = {
    b.entry(
      spec.factory.typesKey,
      _.obj { b =>
        traverse(
          ordering.sorted(types.map {
            case s: AnyShape       => RamlNamedTypeEmitter(s, ordering, references, spec.factory.typesEmitter)
            case r: RecursiveShape => RamlRecursiveShapeTypeEmitter(r, ordering, references)
            case _                 => throw new Exception("Cannot emit non WebApi shape")
          }),
          b
        )
      }
    )
  }

}

case class OasDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends DeclaredTypesEmitters(types, references, ordering) {
  override def emitTypes(b: EntryBuilder): Unit = {
    b.entry("definitions",
            _.obj(traverse(ordering.sorted(types.map(OasNamedTypeEmitter(_, ordering, references))), _)))
  }

}

abstract class DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)

  def emitTypes(b: EntryBuilder): Unit

  override final def emit(b: EntryBuilder): Unit = {
    spec.runAsDeclarations(() => emitTypes(b)) // todo : extract this to "Declaration emitter"?? and set the boolean to false from root emition?
  }

}
