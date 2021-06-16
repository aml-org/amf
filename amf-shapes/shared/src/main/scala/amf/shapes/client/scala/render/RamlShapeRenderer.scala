package amf.shapes.client.scala.render

import amf.core.client.scala.AMFGraphConfiguration
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.raml.emitter.RamlDatatypeSerializer

object RamlShapeRenderer extends RamlDatatypeSerializer {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  override def toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String =
    super.toRamlDatatype(element, config)
}
