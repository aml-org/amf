package amf.dialects
import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}

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
}

object JSDialectRegistry {
  def apply(platform: Platform): JSDialectRegistry = new JSDialectRegistry(platform)
}