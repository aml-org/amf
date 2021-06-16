package amf.apicontract.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.domain.models.Example

trait SchemaContainer {
  def schema: Shape
  def setSchema(shape: Shape): Shape
  def id: String
  def examples: Seq[Example]
  def removeExamples(): Unit
}
