package amf.client

import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.remote.Hint
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

abstract class BaseClient extends PlatformSecrets {

  def generate(url: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    AMFCompiler(url, platform, hint).build().onComplete(callback(handler, url))

  private def callback(handler: Handler[BaseUnit], url: String)(t: Try[BaseUnit]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }
}
