package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasNamedTypeEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{
  RamlNamedTypeEmitter,
  RamlRecursiveShapeTypeEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  RamlShapeEmitterContext,
  ShapeEmitterContext,
  oas
}
import amf.plugins.domain.shapes.models.AnyShape
import amf.validations.RenderSideValidations.RenderValidation
import org.yaml.model.YDocument.EntryBuilder

case class CompactOasTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: OasLikeShapeEmitterContext)
    extends DeclaredTypesEmitters(types, references, ordering) {

  override def emitTypes(b: EntryBuilder): Unit = {
    if (types.nonEmpty || spec.definitionsQueue.nonEmpty)
      b.entry(
        key,
        _.obj { entryBuilder =>
          val definitionsQueue = spec.definitionsQueue
          types.foreach(definitionsQueue.enqueue)
          while (definitionsQueue.nonEmpty()) {
            val labeledShape = definitionsQueue.dequeue()
            // used to force shape to be emitted with OasTypeEmitter, and not as a ref
            spec.setForceEmission(Some(labeledShape.shape.id))
            OasNamedTypeEmitter(labeledShape.shape,
                                ordering,
                                references,
                                pointer = Seq("definitions"),
                                Some(labeledShape.label))
              .emit(entryBuilder)
          }
        }
      )
  }
}

abstract class DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)

  // TODO: THIS SHOULD BE PART OF A SpecSettings object or something of the sort that the context has and we could access.
  val key: String = spec.vendor match {
    case Vendor.OAS30 | Vendor.ASYNC20 => "schemas"
    case Vendor.JSONSCHEMA if spec.isJsonSchema =>
      spec.asInstanceOf[OasLikeShapeEmitterContext].schemasDeclarationsPath.replace("/", "")
    case _ => "definitions"
  }

  def emitTypes(b: EntryBuilder): Unit

  override final def emit(b: EntryBuilder): Unit = {
    spec.runAsDeclarations(() => emitTypes(b)) // todo : extract this to "Declaration emitter"?? and set the boolean to false from root emition?
  }

}

case class RamlDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit spec: RamlShapeEmitterContext)
    extends DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering) {

  override def emitTypes(b: EntryBuilder): Unit = {
    b.entry(
      spec.typesKey,
      _.obj { b =>
        traverse(
          ordering.sorted(types.flatMap {
            case s: AnyShape       => Some(RamlNamedTypeEmitter(s, ordering, references, spec.typesEmitter))
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
    implicit spec: OasLikeShapeEmitterContext)
    extends DeclaredTypesEmitters(types, references, ordering) {

  override def emitTypes(b: EntryBuilder): Unit = {
    if (types.nonEmpty)
      b.entry(
        key,
        _.obj(
          traverse(ordering.sorted(types.map(oas.OasNamedTypeEmitter(_, ordering, references, pointer = Seq(key)))),
                   _))
      )
  }
}
