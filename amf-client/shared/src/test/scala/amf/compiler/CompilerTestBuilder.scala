package amf.compiler

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Hint}
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CompilerTestBuilder extends PlatformSecrets {

  protected def build(url: String, hint: Hint, cache: Option[Cache] = None): Future[BaseUnit] =
    compiler(url, hint, cache).flatMap(_.build())

  protected def compiler(url: String, hint: Hint, cache: Option[Cache] = None): Future[AMFCompiler] = {
    Validation(platform).map { v =>
      AMFCompiler(url, platform, hint, v, cache = cache)
    }
  }
}
