package amf.compiler

import amf.client.environment.WebAPIConfiguration
import amf.client.parse.DefaultErrorHandler
import amf.core.errorhandling.AMFErrorHandler
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
                      validation: Option[Validation] = None,
                      eh: Option[AMFErrorHandler] = None): Future[BaseUnit] =
    compiler(url, hint, cache, resolveValidation(validation), eh).flatMap(_.build())

  private def resolveValidation(validation: Option[Validation]) = validation match {
    case Some(v) => Future { v }
    case _       => Validation(platform)
  }

  protected def compiler(url: String, hint: Hint): Future[AMFCompiler] =
    compiler(url, hint, validation = Validation(platform))

  protected def compiler(url: String,
                         hint: Hint,
                         cache: Option[Cache] = None,
                         validation: Future[Validation],
                         eh: Option[AMFErrorHandler] = None): Future[AMFCompiler] =
    validation.map(v => {
      val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => eh.getOrElse(DefaultErrorHandler()))
      AMFCompiler(url, platform, hint, cache = cache, config = config)
    })
}
