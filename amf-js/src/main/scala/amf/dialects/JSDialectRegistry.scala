package amf.dialects
import amf.facades.{AMFCompiler, Validation}
import amf.core.remote.{ExtensionYamlHint, Platform}
import amf.plugins.document.vocabularies.core.{DialectLoader, PlatformDialectRegistry}
import amf.plugins.document.vocabularies.spec.Dialect

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

class JSDialectRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  def registerDialect(uri: String): Future[Dialect] = {
    val currentValidation = new Validation(platform)
    AMFCompiler(uri, platform, ExtensionYamlHint, currentValidation)
      .build()
      .map { compiled =>
        val dialect = new DialectLoader(compiled).loadDialect()
        add(dialect)
        dialect
      }
  }

  override def registerDialect(url: String, dialectCode: String): Future[Dialect] = {
    platform.cacheResourceText(url, dialectCode)
    val res = registerDialect(url)
    platform.removeCacheResourceText(url)
    res
  }

  @JSExport
  def register(url: String): js.Promise[Dialect] = registerDialect(url).toJSPromise
  @JSExport
  def register(url: String, dialectCode: String): js.Promise[Dialect] = registerDialect(url, dialectCode).toJSPromise
}

object JSDialectRegistry {
  def apply(platform: Platform): JSDialectRegistry = new JSDialectRegistry(platform)
}
