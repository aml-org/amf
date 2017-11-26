package amf.plugins.document.vocabularies.registries

import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.spec.Dialect

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport


object AMFDialectsRegistry extends PlatformSecrets {

  @JSExport
  def register(url: String): js.Promise[Dialect] = PlatformDialectRegistry.registerDialect(url).toJSPromise
  @JSExport
  def register(url: String, dialectCode: String): js.Promise[Dialect] = PlatformDialectRegistry.registerDialect(url, dialectCode).toJSPromise
}
