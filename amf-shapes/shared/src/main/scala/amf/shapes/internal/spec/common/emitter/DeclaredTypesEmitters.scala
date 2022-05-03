package amf.shapes.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.RenderSideValidations.RenderValidation
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.oas.emitter
import amf.shapes.internal.spec.raml.emitter.{RamlNamedTypeEmitter, RamlRecursiveShapeTypeEmitter}
import org.yaml.model.YDocument.EntryBuilder

case class CompactOasTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(implicit
    spec: OasLikeShapeEmitterContext
) extends DeclaredTypesEmitters(types, references, ordering) {

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
            emitter
              .OasNamedTypeEmitter(
                labeledShape.shape,
                ordering,
                references,
                pointer = Seq("definitions"),
                Some(labeledShape.label)
              )
              .emit(entryBuilder)
          }
        }
      )
  }
}

abstract class DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {
  override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)

  // TODO: THIS SHOULD BE PART OF A SpecSettings object or something of the sort that the context has and we could access.
  val key: String = spec.spec match {
    case Spec.OAS30 | Spec.ASYNC20 => "schemas"
    case Spec.JSONSCHEMA if spec.isJsonSchema =>
      spec.asInstanceOf[OasLikeShapeEmitterContext].schemasDeclarationsPath.replace("/", "")
    case _ => "definitions"
  }

  def emitTypes(b: EntryBuilder): Unit

  override final def emit(b: EntryBuilder): Unit = {
    spec.runAsDeclarations(() =>
      emitTypes(b)
    ) // todo : extract this to "Declaration emitter"?? and set the boolean to false from root emition?
  }

}

case class RamlDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(implicit
    spec: RamlShapeEmitterContext
) extends DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering) {

  override def emitTypes(b: EntryBuilder): Unit = {
    b.entry(
      spec.typesKey,
      _.obj { b =>
        traverse(
          ordering.sorted(types.flatMap {
            case s: AnyShape       => Some(RamlNamedTypeEmitter(s, ordering, references, spec.typesEmitter))
            case r: RecursiveShape => Some(RamlRecursiveShapeTypeEmitter(r, ordering, references))
            case other =>
              spec.eh.violation(
                RenderValidation,
                other.id,
                None,
                "Cannot emit non WebApi shape",
                other.position(),
                other.location()
              )
              None
          }),
          b
        )
      }
    )
  }
}

case class OasDeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(implicit
    spec: OasLikeShapeEmitterContext
) extends DeclaredTypesEmitters(types, references, ordering) {

  override def emitTypes(b: EntryBuilder): Unit = {
    if (types.nonEmpty)
      b.entry(
        key,
        _.obj(
          traverse(
            ordering.sorted(types.map(emitter.OasNamedTypeEmitter(_, ordering, references, pointer = Seq(key)))),
            _
          )
        )
      )
  }
}
