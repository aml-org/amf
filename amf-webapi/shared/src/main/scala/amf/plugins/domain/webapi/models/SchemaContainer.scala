package amf.plugins.domain.webapi.models

import amf.core.model.domain.Shape
import amf.plugins.domain.shapes.models.Example

trait SchemaContainer {
  def schema: Shape
  def setSchema(shape: Shape): Shape
  def hasSchema(): Boolean = Option(schema).isDefined
  def id: String
  def examples: Seq[Example]
  def removeExamples(): Unit
}
