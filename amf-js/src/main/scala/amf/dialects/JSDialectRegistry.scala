package amf.dialects
import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}
import amf.spec.dialects.Dialect

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

class JSDialectRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  def registerDialect(uri: String) =  {
    AMFCompiler(uri, platform, RamlYamlHint)
      .build()
      .map { compiled =>
        val dialect = new DialectLoader().loadDialect(compiled)
        add(dialect)
        dialect
      }
  }

  override def registerDialect(url: String, dialectCode: String) = {
    platform.cacheResourceText(url, dialectCode)
    val res = registerDialect(url)
    platform.removeCacheResourceText(url)
    res
  }

  @JSExport
  def register(url:String): js.Promise[Dialect] = registerDialect(url).toJSPromise
  @JSExport
  def register(url: String, dialectCode: String): js.Promise[Dialect] = registerDialect(url, dialectCode).toJSPromise
}

object JSDialectRegistry {
  def apply(platform: Platform): JSDialectRegistry = new JSDialectRegistry(platform)
}