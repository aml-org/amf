package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.exported.AMFGraphConfiguration
import amf.client.model.domain.AnyShape
import amf.plugins.document.apicontract.parser.spec.common.RamlDatatypeSerializer

object ClientRamlShapeSerializer extends RamlDatatypeSerializer {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  def toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String = super.toRamlDatatype(element, config)
}
