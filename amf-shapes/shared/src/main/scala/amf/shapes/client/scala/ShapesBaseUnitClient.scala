package amf.shapes.client.scala

import amf.aml.client.scala.AMLBaseUnitClient
import amf.core.client.scala.parse.AMFParser

import scala.concurrent.ExecutionContext

/**
  * The AMF Client contains common AMF operations associated to base unit and documents.
  * For more complex uses see [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
// Left here so that we can add behaviour if necessary without breaking interface
class ShapesBaseUnitClient private[amf] (override protected val configuration: ShapesConfiguration)
    extends AMLBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: ShapesConfiguration = configuration
}
