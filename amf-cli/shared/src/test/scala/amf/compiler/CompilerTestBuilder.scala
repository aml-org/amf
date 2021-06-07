package amf.compiler

import amf.client.environment.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.AMFCompiler
import amf.core.errorhandling.UnhandledErrorHandler
import amf.client.environment.WebAPIConfiguration
import amf.client.errorhandling.DefaultErrorHandler
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context, Hint}
import amf.core.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CompilerTestBuilder extends PlatformSecrets {

  protected def defaultConfig: AMFConfiguration =
    WebAPIConfiguration
      .WebAPI()
      .merge(AsyncAPIConfiguration.Async20())
      .withErrorHandlerProvider(() => UnhandledErrorHandler)

  protected def build(url: String, hint: Hint, amfConfig: AMFConfiguration, cache: Option[Cache]): Future[BaseUnit] =
    compiler(url, hint, amfConfig, cache).build()

  protected def build(url: String, hint: Hint, cache: Option[Cache] = None): Future[BaseUnit] =
    build(url, hint, defaultConfig, cache)

  protected def compiler(url: String, hint: Hint, amfConfig: AMFConfiguration, cache: Option[Cache]): AMFCompiler =
    AMFCompiler(
      url,
      Some(hint.vendor.mediaType + "+" + hint.syntax.extension),
      cache = cache.getOrElse(Cache()),
      parserConfig = amfConfig.parseConfiguration,
      base = Context(platform)
    )

  protected def compiler(url: String, hint: Hint): AMFCompiler = {
    compiler(url, hint, defaultConfig, None)
  }
}
