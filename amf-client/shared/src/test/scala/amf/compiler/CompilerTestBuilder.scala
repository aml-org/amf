package amf.compiler

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Hint}
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CompilerTestBuilder extends PlatformSecrets {

  protected def build(url: String,
                      hint: Hint,
                      cache: Option[Cache] = None,
                      validation: Option[Validation] = None): Future[BaseUnit] =
    compiler(url, hint, cache, resolveValidation(validation)).flatMap(_.build())

  private def resolveValidation(validation: Option[Validation]) = validation match {
    case Some(v) => Future { v }
    case _       => Validation(platform)
  }

  protected def compiler(url: String, hint: Hint): Future[AMFCompiler] =
    compiler(url, hint, validation = Validation(platform))

  protected def compiler(url: String,
                         hint: Hint,
                         cache: Option[Cache] = None,
                         validation: Future[Validation]): Future[AMFCompiler] =
    validation.map(v => AMFCompiler(url, platform, hint, v, cache = cache))
}
