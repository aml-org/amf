package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape, UnionShape}

case class AvroShapeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: AvroShapeEmitterContext) {

  def entries(): Seq[EntryEmitter] = {
    shape match {
      case map: NodeShape if isMapShape(map) => AvroMapShapeEmitter(map, ordering).emitters()
      case node: NodeShape                   => AvroRecordShapeEmitter(node, ordering).emitters()
      case array: ArrayShape                 => AvroArrayShapeEmitter(array, ordering).emitters()
      case scalar: ScalarShape               => handleScalarShape(scalar)
      case union: UnionShape                 => AvroUnionShapeEmitter(union, ordering).emitters()
      case property: PropertyShape           => AvroPropertyShapeEmitter(property, ordering).emitters()
      case _                                 => Seq()
    }
  }

  private def handleScalarShape(scalar: ScalarShape): Seq[EntryEmitter] = {
    spec.getAvroType(scalar) match {
      case Some("fixed") => AvroFixedShapeEmitter(scalar, ordering).emitters()
      case Some("enum")  => AvroEnumShapeEmitter(scalar, ordering).emitters()
      case _             => AvroScalarShapeEmitter(scalar, ordering).emitters() // for primitives
    }
  }
  private def isMapShape(node: NodeShape): Boolean = spec.getAvroType(node).contains("map")

}
