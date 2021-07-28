package amf.apicontract.internal.spec.async

import amf.apicontract.internal.plugins.ApiElementRenderPlugin
import amf.apicontract.internal.spec.async.emitters.domain.AsyncDomainElementEmitterFactory
import amf.apicontract.internal.spec.common.emitter.DomainElementEmitterFactory
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.remote.SpecId

object Async20ElementRenderPlugin extends ApiElementRenderPlugin {

  override protected def vendor: SpecId = SpecId.ASYNC20

  override protected def emitterFactory: AMFErrorHandler => DomainElementEmitterFactory =
    eh => AsyncDomainElementEmitterFactory(eh)
}
