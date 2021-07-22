package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.plugins.ApiElementRenderPlugin
import amf.apicontract.internal.spec.common.emitter.DomainElementEmitterFactory
import amf.apicontract.internal.spec.oas.emitter.domain.Oas20EmitterFactory
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.remote.Vendor

object Oas20ElementRenderPlugin extends ApiElementRenderPlugin {

  override protected def vendor: Vendor = Vendor.OAS20

  override protected def emitterFactory: AMFErrorHandler => DomainElementEmitterFactory = eh => Oas20EmitterFactory(eh)
}
