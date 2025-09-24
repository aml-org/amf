package amf.othercard.internal.plugins.render

import amf.core.internal.remote.{OtherCard, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object OtherCardRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = OtherCard

}
