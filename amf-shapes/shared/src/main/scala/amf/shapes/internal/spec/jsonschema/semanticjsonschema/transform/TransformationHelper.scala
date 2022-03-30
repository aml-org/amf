package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMapping
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

object TransformationHelper {

  def dummyMapping(shape: AnyShape): NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def dummyMapping(id: String): NodeMapping = NodeMapping().withId(id)

  def dummyShape(id: String): NodeShape = NodeShape().withId(id)

  def emptyMapping(id: String): NodeMapping = NodeMapping().withId(id).withClosed(false)

}
