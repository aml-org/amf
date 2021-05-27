package amf.facades

import amf.client.remod.amfcore.config.ParsingOptionsConverter
import amf.client.remod.{AMFGraphConfiguration, ParseConfiguration}
import amf.core.client.ParsingOptions
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Syntax.{Json, PlainText, Yaml}
import amf.core.remote._
import amf.core.{CompilerContext, CompilerContextBuilder, Root, AMFCompiler => ModularCompiler}
import amf.plugins.document.vocabularies.plugin.headers.RamlHeaderExtractor

import scala.concurrent.{ExecutionContext, Future}

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           private val cache: Cache,
                           newConfiguration: AMFGraphConfiguration)(implicit executionContext: ExecutionContext)
    extends RamlHeaderExtractor {

  private val compilerContext: CompilerContext = {
    val builder =
      new CompilerContextBuilder(url, remote, ParseConfiguration(newConfiguration)).withCache(cache)
    base.foreach(builder.withFileContext)
    builder.build()
  }

  def build(): Future[BaseUnit] = {

    val mediaType = hint.syntax match {
      case Yaml      => Some("application/yaml")
      case Json      => Some("application/json")
      case PlainText => Some("text/plain") // we cannot parse this?
      case _         => None
    }

    new ModularCompiler(
      compilerContext,
      mediaType
    ).build()
  }

  def root(): Future[Root] = {

    val mediaType = hint.syntax match {
      case Yaml      => Some("application/yaml")
      case Json      => Some("application/json")
      case PlainText => Some("text/plain") // we cannot parse this?
      case _         => None
    }

    new ModularCompiler(
      compilerContext,
      mediaType
    ).root()
  }

}

object AMFCompiler {
  // interface used by some testing classes, ideally will be removed
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            ctx: Option[ParserContext] = None,
            config: AMFGraphConfiguration)(implicit executionContext: ExecutionContext): AMFCompiler = {
    new AMFCompiler(url, remote, context, hint, cache.getOrElse(Cache()), config)
  }

}
