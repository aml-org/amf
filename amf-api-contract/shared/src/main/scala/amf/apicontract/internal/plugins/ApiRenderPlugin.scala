package amf.apicontract.internal.plugins

import amf.core.internal.plugins.render.SYAMLBasedRenderPlugin
import amf.core.internal.remote.Spec

trait ApiRenderPlugin extends SYAMLBasedRenderPlugin {

  def spec: Spec

  override val id: String = spec.id
}
