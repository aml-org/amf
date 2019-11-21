package amf.plugins.domain.webapi.models

import amf.core.model.domain.Shape
import amf.plugins.domain.shapes.models.Example

trait SchemaContainer {
  def schema: Shape
  def examples: Seq[Example]
  def removeExamples(): Unit
}