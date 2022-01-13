package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.plugins.ApiElementRenderPlugin
import amf.apicontract.internal.spec.common.emitter.DomainElementEmitterFactory
import amf.apicontract.internal.spec.oas.emitter.domain.Oas30EmitterFactory
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec

object Oas30ElementRenderPlugin extends ApiElementRenderPlugin {

  override protected def spec: Spec = Spec.OAS30

  override protected def emitterFactory: (AMFErrorHandler, RenderConfiguration) => DomainElementEmitterFactory =
    (eh, config) => Oas30EmitterFactory(eh, config)
}
