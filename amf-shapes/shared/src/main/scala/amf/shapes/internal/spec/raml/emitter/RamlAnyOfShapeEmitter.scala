package amf.shapes.internal.spec.raml.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.domain.metamodel.UnionShapeModel
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

abstract class AnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends Emitter {

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

case class RamlAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends AnyOfShapeEmitter(shape, ordering, references)
    with EntryEmitter {

  override def emit(b: EntryBuilder): Unit = typeExpression match {
    case Some(e) => emitUnionInlined(e, b)
    case None    => emitUnionExpanded(b)
  }

  def emitUnionInlined(expression: String, b: EntryBuilder): Unit = b.entry("type", expression)
}

case class RamlInlinedAnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends AnyOfShapeEmitter(shape, ordering, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit = typeExpression match {
    case Some(e) => emitUnionInlined(e, b)
    case None    => emitUnionExpanded(b)
  }

  def emitUnionExpanded(b: PartBuilder): Unit = b.obj(b => emitUnionExpanded(b))

  def emitUnionInlined(expression: String, b: PartBuilder): Unit = b += expression
}
