package amf.core.services

import amf.core.client.ParsingOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParserContext, ReferenceKind, UnspecifiedReference}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{Cache, Context}
import amf.internal.environment.Environment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RuntimeCompiler {
  def build(url: String,
            base: Context,
            mediaType: Option[String],
            vendor: Option[String],
            referenceKind: ReferenceKind,
            cache: Cache,
            ctx: Option[ParserContext],
            env: Environment = Environment(),
            parsingOptions: ParsingOptions = ParsingOptions()): Future[BaseUnit]
}

object RuntimeCompiler {
  var compiler: Option[RuntimeCompiler] = None
  def register(runtimeCompiler: RuntimeCompiler): Unit = {
    compiler = Some(runtimeCompiler)
  }

  def apply(url: String,
            mediaType: Option[String],
            vendor: Option[String],
            base: Context,
            referenceKind: ReferenceKind = UnspecifiedReference,
            cache: Cache,
            ctx: Option[ParserContext] = None,
            env: Environment = Environment(),
            parsingOptions: ParsingOptions = ParsingOptions()): Future[BaseUnit] = {
    compiler match {
      case Some(runtimeCompiler) =>
        AMFPluginsRegistry.featurePlugins().foreach(_.onBeginParsingInvocation(url, mediaType))
        runtimeCompiler.build(url, base, mediaType, vendor, referenceKind, cache, ctx, env, parsingOptions) map {
          parsedUnit =>
            AMFPluginsRegistry.featurePlugins().foldLeft(parsedUnit) {
              case (parsed, plugin) =>
                plugin.onFinishedParsingInvocation(url, parsed)
            }
        }
      case _ => throw new Exception("No registered runtime compiler")
    }
  }
}
