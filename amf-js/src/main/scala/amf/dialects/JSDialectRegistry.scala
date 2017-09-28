package amf.dialects
import amf.compiler.AMFCompiler
import amf.remote.{Context, Platform, RamlYamlHint}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

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
}

object JSDialectRegistry {
  def apply(platform: Platform): JSDialectRegistry = new JSDialectRegistry(platform)
}