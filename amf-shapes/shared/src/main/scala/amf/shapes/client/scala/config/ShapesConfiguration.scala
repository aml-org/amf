package amf.shapes.client.scala.config

import amf.aml.client.scala.AMLConfiguration
import amf.shapes.internal.annotations.ShapeSerializableAnnotations
import amf.shapes.internal.entities.ShapeEntities

object ShapesConfiguration {

  def predefined(): AMLConfiguration = {
    // TODO ARM: validate plugin and payload plugin of api?
    AMLConfiguration
      .predefined()
      .withEntities(ShapeEntities.entities)
      .withAnnotations(ShapeSerializableAnnotations.annotations)
  }
}
