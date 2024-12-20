package amf.compiler

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.parser.AMFCompiler
import amf.core.internal.remote.{Cache, Context, Hint}

import scala.concurrent.Future

trait CompilerTestBuilder extends AsyncFunSuiteWithPlatformGlobalExecutionContext {

  protected def defaultConfig: AMFConfiguration =
    APIConfiguration
      .API()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)

  protected def build(url: String, hint: Hint, amfConfig: AMFConfiguration, cache: Option[Cache]): Future[BaseUnit] =
    compiler(url, hint, amfConfig, cache).build()

  protected def build(url: String, hint: Hint, cache: Option[Cache] = None): Future[BaseUnit] =
    build(url, hint, defaultConfig, cache)

  protected def compiler(url: String, hint: Hint, amfConfig: AMFConfiguration, cache: Option[Cache]): AMFCompiler =
    AMFCompiler(
      url,
      cache = cache.getOrElse(Cache()),
      parserConfig = amfConfig.compilerConfiguration,
      base = Context(platform)
    )

  protected def compiler(url: String, hint: Hint): AMFCompiler = {
    compiler(url, hint, defaultConfig, None)
  }
}
