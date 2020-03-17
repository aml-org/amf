package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models.UnionShape
import org.yaml.model.YDocument.PartBuilder

case class RamlInlinedAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    RamlUnionEmitterHelper.inlinedEmission(shape) match {
      case Some(e) => emitUnionInlined(e, b)
      case None    => emitUnionExpanded(b)
    }
  }

  def emitUnionExpanded(b: PartBuilder): Unit = {
    b.obj(
      b =>
        b.entry(
          "anyOf",
          _.list { b =>
            val emitters = shape.anyOf.map(s =>
              declaration.emitters.Raml10TypePartEmitter(s, ordering, None, references = references))
            // TODO add lexical information to anyOf elements in TypeExpressionParser. As a WA, the emitters are sorted by the shape id.
            traverse(ordering.sorted(emitters), b)
          }
      )
    )
  }

  def emitUnionInlined(types: String, b: PartBuilder): Unit = b += types

  override def position(): Position = pos(shape.fields.get(UnionShapeModel.AnyOf).annotations)
}
