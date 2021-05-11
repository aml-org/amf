package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{OasLikeShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, TupleShapeModel}
import amf.plugins.domain.shapes.models.TupleShape
import org.yaml.model.YDocument.EntryBuilder

case class OasTupleItemsShapeEmitter(
    array: TupleShape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    additionalEntry: Option[ValueEmitter],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  val itemEmitters: Seq[OasTypeEmitter] = {
    array.items.zipWithIndex.map {
      case (shape, i) =>
        /*
      val collector = new SimpleOasTypePartCollector(shape, ordering, Nil, references)
      collector.computeEmitters(pointer ++ Seq("items", s"$i"), schemaPath)
         */
        OasTypeEmitter(shape, ordering, Nil, references, pointer ++ Seq("items", s"$i"), schemaPath)
    }
  }

  def emit(b: EntryBuilder): Unit = {
    if (Option(array.fields.getValue(TupleShapeModel.TupleItems)).isDefined) {
      b.entry(
        "items",
        _.list { le =>
          itemEmitters.foreach { emitter =>
            val allEmitters = emitter.emitters().collect { case e: EntryEmitter => e }
            le.obj { o =>
              allEmitters.foreach(_.emit(o))
            }
          }
        }
      )
    }
  }

  override def position(): Position = {
    Option(array.fields.getValue(ArrayShapeModel.Items)) match {
      case Some(value) => pos(value.annotations)
      case _           => ZERO
    }
  }
}
