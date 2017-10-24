package amf.client

import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

private[client] abstract class PlatformParser extends PlatformSecrets {

  protected val vendor: Vendor
  protected val syntax: Syntax

  protected def parseAsync(url: String, overridePlatForm: Option[Platform] = None): Future[BaseUnit] = {
    val effectivePlatform = overridePlatForm.getOrElse(platform)
    AMFCompiler(url, effectivePlatform, hint(), None, None, effectivePlatform.dialectsRegistry).build()
  }


  protected def parse(url: String, handler: Handler[BaseUnit], overridePlatForm: Option[Platform] = None): Unit =
    parseAsync(url, overridePlatForm)
      .onComplete(callback(handler, url))

  private def hint(): Hint = {
    (vendor, syntax) match {
      case (Raml, Yaml) => RamlYamlHint
      case (Oas, Json)  => OasJsonHint
      case (Amf, Json)  => AmfJsonHint
      case _            => throw new RuntimeException(s"Unable conbination of vendor '$vendor' and syntax '$syntax'")
    }
  }

  private def callback(handler: Handler[BaseUnit], url: String)(t: Try[BaseUnit]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }
}
