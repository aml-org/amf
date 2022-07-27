package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.ArrayShape
import amf.shapes.internal.domain.metamodel.ArrayShapeModel
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class OasItemsShapeEmitter(
    array: ArrayShape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    additionalEntry: Option[ValueEmitter],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil
)(implicit spec: OasLikeShapeEmitterContext)
    extends OasTypePartCollector(array.items, ordering, Nil, references)
    with EntryEmitter {

  def emit(b: EntryBuilder): Unit = {
    if (Option(array.fields.getValue(ArrayShapeModel.Items)).isDefined) {
      b.entry("items", b => emitPart(b))
    }
  }

  def emitPart(part: PartBuilder): Unit = {
    emitter(pointer :+ "items", schemaPath) match {
      case Left(p)        => p.emit(part) // What happens if additionalProperty is defined and is not an Seq?
      case Right(entries) => part.obj(traverse(entries ++ additionalEntry, _))
    }
  }

  override def position(): Position = {
    Option(array.fields.getValue(ArrayShapeModel.Items)) match {
      case Some(value) =>
        pos(value.value.annotations)
      case _ => ZERO
    }
  }
}
