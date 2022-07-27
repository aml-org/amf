package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.federation.ShapeFederationMetadata
import amf.shapes.client.scala.model.domain.operations.{ShapeParameter => InternalShapeParameter}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapeParameter(override private[amf] val _internal: InternalShapeParameter)
    extends AbstractParameter(_internal) {

  def federationMetadata: ShapeFederationMetadata = _internal.federationMetadata

  def withFederationMetadata(metadata: ShapeFederationMetadata): this.type = {
    _internal.withFederationMetadata(metadata)
    this
  }

  @JSExportTopLevel("ShapeParameter")
  def this() = this(InternalShapeParameter())

}
