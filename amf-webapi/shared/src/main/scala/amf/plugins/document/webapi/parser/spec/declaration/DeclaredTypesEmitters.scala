package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{SpecOrdering, EntryEmitter}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.models.AnyShape
import amf.validations.RenderSideValidations.RenderValidation
import org.yaml.model.YDocument.EntryBuilder

case class RamlDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: RamlSpecEmitterContext)
    extends DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering) {
  override def emitTypes(b: EntryBuilder): Unit = {
    b.entry(
      spec.factory.typesKey,
      _.obj { b =>
        traverse(
          ordering.sorted(types.flatMap {
            case s: AnyShape       => Some(RamlNamedTypeEmitter(s, ordering, references, spec.factory.typesEmitter))
            case r: RecursiveShape => Some(RamlRecursiveShapeTypeEmitter(r, ordering, references))
            case other =>
              spec.eh.violation(RenderValidation,
                                other.id,
                                None,
                                "Cannot emit non WebApi shape",
                                other.position(),
                                other.location())
              None
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
    val isOas3 = spec.vendor == Vendor.OAS30
    b.entry(
      if (isOas3) "schemas" else "definitions",
      _.obj(
        traverse(
          ordering.sorted(types.map(OasNamedTypeEmitter(_, ordering, references, pointer = Seq("definitions")))),
          _))
    )
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
