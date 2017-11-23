package amf.framework.services

import amf.framework.document.BaseUnit
import amf.framework.parser.{ReferenceKind, Unspecified}
import amf.remote.{Cache, Context, Platform}
import amf.spec.ParserContext
import amf.validation.Validation

import scala.concurrent.Future

trait RuntimeCompiler {
  def build(url: String,
            remote: Platform,
            base: Option[Context],
            mediaType: String,
            vendor: String,
            currentValidation: Validation,
            referenceKind: ReferenceKind,
            cache: Cache,
            ctx: Option[ParserContext]): Future[BaseUnit]
}

object RuntimeCompiler {
  var compiler: Option[RuntimeCompiler] = None
  def register(runtimeCompiler: RuntimeCompiler) = {
    compiler = Some(runtimeCompiler)
  }

  def apply(url: String,
            remote: Platform,
            mediaType: String,
            vendor: String,
            currentValidation: Validation,
            base: Option[Context] = None,
            referenceKind: ReferenceKind = Unspecified,
            cache: Cache = Cache(),
            ctx: Option[ParserContext] = None) = {
    compiler match {
      case Some(runtimeCompiler) => runtimeCompiler.build(url, remote, base, mediaType, vendor, currentValidation, referenceKind, cache, ctx)
      case _                     => throw new Exception("No registered runtime compiler")
    }
  }
}
