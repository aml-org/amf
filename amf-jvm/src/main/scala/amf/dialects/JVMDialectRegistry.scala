package amf.dialects

import java.util.concurrent.CompletableFuture

import amf.compiler.AMFCompiler
import amf.remote.{ExtensionYamlHint, Platform, RamlYamlHint}
import amf.spec.dialects.Dialect
import amf.remote.FutureConverter.converters
import amf.validation.Validation

import scala.concurrent.ExecutionContext.Implicits.global

class JVMDialectRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  override def registerDialect(uri: String) =  {
    val currentValidaton = new Validation(platform).withEnabledValidation(false)
    AMFCompiler(uri, platform, ExtensionYamlHint, currentValidaton)
      .build()
      .map { compiled =>
        val dialect = new DialectLoader(compiled).loadDialect()
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

  def register(url:String): CompletableFuture[Dialect] = registerDialect(url).asJava
  def register(url: String, dialectCode: String): CompletableFuture[Dialect] = registerDialect(url, dialectCode).asJava
}

object JVMDialectRegistry {
  def apply(platform: Platform): JVMDialectRegistry = new JVMDialectRegistry(platform)
}
