package amf.dialects

import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}

import  scala.concurrent.ExecutionContext.Implicits.global

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
}

object JVMDialectRegistry {
  def apply(platform: Platform): JVMDialectRegistry = new JVMDialectRegistry(platform)
}
