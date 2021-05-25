package amf.remod

import amf.client.execution.BaseExecutionEnvironment
import amf.client.remod.AMFGraphConfiguration
import amf.plugins.document.webapi.parser.spec.common.RamlDatatypeSerializer
import amf.plugins.domain.shapes.models.AnyShape

object RamlShapeSerializer extends RamlDatatypeSerializer {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  override def toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String =
    super.toRamlDatatype(element, config)
}
