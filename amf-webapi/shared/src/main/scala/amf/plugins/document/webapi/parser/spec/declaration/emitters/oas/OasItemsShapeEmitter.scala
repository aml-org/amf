package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel
import amf.plugins.domain.shapes.models.ArrayShape
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class OasItemsShapeEmitter(array: ArrayShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                additionalEntry: Option[ValueEmitter],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
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
