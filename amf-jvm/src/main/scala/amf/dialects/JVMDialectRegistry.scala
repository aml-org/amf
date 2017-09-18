package amf.dialects
import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}

import  scala.concurrent.ExecutionContext.Implicits.global

class JVMDialectRegistry extends PlatformDialectRegistry {
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
