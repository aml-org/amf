package amf.dialects
import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

class JSDialectRegistry extends PlatformDialectRegistry {
  override def add(p: Platform, uri: String) = {
    AMFCompiler(uri, p, RamlYamlHint)
      .build()
      .map { compiled =>
        val dialect = new DialectLoader().loadDialect(compiled)
        add(dialect)
        dialect
      }
  }
}
