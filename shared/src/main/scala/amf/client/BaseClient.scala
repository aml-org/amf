package amf.client

import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.remote._
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

abstract class BaseClient extends PlatformSecrets {

  def generate(url: String, hint: Hint, handler: Handler[BaseUnit], overridePlatForm: Option[Platform] = None): Unit =
    AMFCompiler(url, if (overridePlatForm.isDefined) overridePlatForm.get else platform, hint)
      .build()
      .onComplete(callback(handler, url))

  private def callback(handler: Handler[BaseUnit], url: String)(t: Try[BaseUnit]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }

  protected def matchSourceHint(source: String): Hint = {
    source match {
      case "json" | "oas" | "openapi" => OasJsonHint
      case "raml" | "yaml"            => RamlYamlHint
      case _                          => AmfJsonLdHint
    }
  }

  protected def matchToVendor(toVendor: String): Vendor = {
    toVendor match {
      case "json" | "oas" | "openapi" => Oas
      case "raml" | "yaml"            => Raml
      case _                          => Amf
    }
  }
}

trait Client[T] {
  def generateFromFile(url: String, hint: Hint, handler: Handler[T]): Unit

  def generateFromStream(stream: String, hint: Hint, handler: Handler[T]): Unit
}
