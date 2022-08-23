package amf.graphqlfederation.internal.spec.transformation.introspection

import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NilShape, UnionShape}

object TypeBuilders {

  def nullable(shape: AnyShape): UnionShape = {
    UnionShape()
      .withAnyOf(
        List(
          shape,
          NilShape()
        )
      )
  }

  def array(shape: AnyShape): ArrayShape = ArrayShape().withItems(shape)
}
