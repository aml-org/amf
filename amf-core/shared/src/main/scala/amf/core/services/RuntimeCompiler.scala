package amf.core.services

import amf.core.model.document.BaseUnit
import amf.core.parser.{ParserContext, ReferenceKind, UnspecifiedReference}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{Cache, Context, Platform}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RuntimeCompiler {
  def build(url: String,
            remote: Platform,
            base: Option[Context],
            mediaType: Option[String],
            vendor: String,
            referenceKind: ReferenceKind,
            cache: Cache,
            ctx: Option[ParserContext]): Future[BaseUnit]
}

object RuntimeCompiler {
  var compiler: Option[RuntimeCompiler] = None
  def register(runtimeCompiler: RuntimeCompiler): Unit = {
    compiler = Some(runtimeCompiler)
  }

  def apply(url: String,
            remote: Platform,
            mediaType: Option[String],
            vendor: String,
            base: Option[Context] = None,
            referenceKind: ReferenceKind = UnspecifiedReference,
            cache: Cache = Cache(),
            ctx: Option[ParserContext] = None): Future[BaseUnit] = {
    compiler match {
      case Some(runtimeCompiler) =>
        AMFPluginsRegistry.featurePlugins().foreach(_.onBeginParsingInvocation(url, mediaType, vendor))
        runtimeCompiler.build(url, remote, base, mediaType, vendor, referenceKind, cache, ctx) map { parsedUnit =>
          AMFPluginsRegistry.featurePlugins().foldLeft(parsedUnit) { case (parsed, plugin) =>
            plugin.onFinishedParsingInvocation(url, parsed)
          }
        }
      case _                     => throw new Exception("No registered runtime compiler")
    }
  }
}
