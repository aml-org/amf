package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.{NilShape, UnionShape}
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroUnionShapeEmitter(
    unionShape: UnionShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(unionShape, ordering) {
  override def emitSpecificFields(b: EntryBuilder): Unit = {
    b.entry(
      "type",
      _.list { lb =>
        unionShape.anyOf.foreach {
          case _: NilShape => lb += "null"
          case complex: Shape if spec.isComplex(complex) =>
            lb.obj { entryBuilder =>
              val shapeEmitter = AvroShapeEmitter(complex, ordering)
              shapeEmitter.entries().foreach(_.emit(entryBuilder))
            }
          case anyOfMember => lb += spec.getAvroType(anyOfMember).getOrElse("default")
        }
      }
    )
  }

  override def position(): Position = pos(unionShape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)

}
