package amf.shapes.internal.spec.oas.emitter

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.TupleShape
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, TupleShapeModel}
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
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
