package amf.brokergroup.internal.plugins.render

import amf.core.internal.remote.{BrokerGroup, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object BrokerGroupRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = BrokerGroup

}
