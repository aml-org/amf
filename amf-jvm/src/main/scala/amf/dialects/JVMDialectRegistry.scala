package amf.dialects

import java.util.concurrent.CompletableFuture

import amf.facades.{AMFCompiler, Validation}
import amf.framework.remote.{ExtensionYamlHint, Platform}
import amf.framework.services.RuntimeValidator
import amf.plugins.document.vocabularies.core.{DialectLoader, PlatformDialectRegistry}
import amf.plugins.document.vocabularies.spec.Dialect
import amf.remote.FutureConverter.converters

import scala.concurrent.ExecutionContext.Implicits.global

class JVMDialectRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  override def registerDialect(uri: String) =  {
    RuntimeValidator.disableValidationsAsync() { reenableValidations =>
      val currentValidaton = new Validation(platform)
      AMFCompiler(uri, platform, ExtensionYamlHint, currentValidaton)
        .build()
        .map { compiled =>
          reenableValidations()
          val dialect = new DialectLoader(compiled).loadDialect()
          add(dialect)
          dialect
        }
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
