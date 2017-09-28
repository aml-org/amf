package amf.dialects

import amf.compiler.AMFCompiler
import amf.remote.{Context, Platform, RamlYamlHint}

import scala.concurrent.ExecutionContext.Implicits.global

class JVMDialectRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  override def registerDialect(uri: String) =  {
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

object JVMDialectRegistry {
  def apply(platform: Platform): JVMDialectRegistry = new JVMDialectRegistry(platform)
}
