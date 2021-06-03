package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models.UnionShape
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

abstract class AnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends Emitter {

  def emitUnionExpanded(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        val emitters = shape.anyOf.map(s => Raml10TypePartEmitter(s, ordering, None, references = references))
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  def typeExpression: Option[String] = RamlUnionEmitterHelper.inlinedEmission(shape)

  override def position(): Position = pos(shape.fields.get(UnionShapeModel.AnyOf).annotations)
}

case class RamlAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends AnyOfShapeEmitter(shape, ordering, references)
    with EntryEmitter {

  override def emit(b: EntryBuilder): Unit = typeExpression match {
    case Some(e) => emitUnionInlined(e, b)
    case None    => emitUnionExpanded(b)
  }

  def emitUnionInlined(expression: String, b: EntryBuilder): Unit = b.entry("type", expression)
}

case class RamlInlinedAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends AnyOfShapeEmitter(shape, ordering, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit = typeExpression match {
    case Some(e) => emitUnionInlined(e, b)
    case None    => emitUnionExpanded(b)
  }

  def emitUnionExpanded(b: PartBuilder): Unit = b.obj(b => emitUnionExpanded(b))

  def emitUnionInlined(expression: String, b: PartBuilder): Unit = b += expression
}
