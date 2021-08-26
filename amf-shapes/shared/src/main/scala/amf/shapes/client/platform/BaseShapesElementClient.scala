package amf.shapes.client.platform

import amf.aml.client.platform.BaseAMLElementClient
import amf.shapes.client.scala.{ShapesElementClient => InternalShapesElementClient}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class BaseShapesElementClient private[amf] (private val _internal: InternalShapesElementClient)
    extends BaseAMLElementClient(_internal) {

  private[amf] def this(configuration: ShapesConfiguration) = {
    this(new InternalShapesElementClient(configuration))
  }

  override def getConfiguration(): ShapesConfiguration = _internal.getConfiguration
}
