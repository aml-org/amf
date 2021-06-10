package amf.client.environment

import amf.plugins.domain.shapes.annotations.serializable.ShapeSerializableAnnotations
import amf.plugins.domain.shapes.entities.ShapeEntities

object ShapesConfiguration {

  def predefined(): AMLConfiguration = {
    // TODO ARM: validate plugin and payload plugin of api?
    AMLConfiguration
      .predefined()
      .withEntities(ShapeEntities.entities)
      .withAnnotations(ShapeSerializableAnnotations.annotations)
  }
}
