package amf.remod

import amf.client.execution.BaseExecutionEnvironment
import amf.client.model.domain.AnyShape
import amf.plugins.document.webapi.parser.spec.common.RamlDatatypeSerializer
import amf.client.convert.shapeconverters.ShapeClientConverters._

object ClientRamlShapeSerializer extends RamlDatatypeSerializer {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  def toRamlDatatype(element: AnyShape, exec: BaseExecutionEnvironment): String = super.toRamlDatatype(element, exec)

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  def toRamlDatatype(element: AnyShape): String = toRamlDatatype(element, platform.defaultExecutionEnvironment)
}
