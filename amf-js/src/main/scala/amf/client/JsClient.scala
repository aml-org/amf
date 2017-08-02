package amf.client

import amf.compiler.AMFCompiler
import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.dumper.AMFDumper
import amf.lexer.CharSequenceStream
import amf.remote._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success, Try}

/**
  *
  */
@JSExportTopLevel("JsClient")
class JsClient extends BaseClient {
  @JSExport
  def generate(url: String, syntax: String, handler: JsHandler[BaseUnit]): Unit = {
    /*generate(url, syntax, new Handler {
            override def success(document: Document): Unit = handler.success(document)
            override def error(exception: Throwable): Unit = handler.error(exception)
        })*/
  }

  @JSExport
  def webApiClass(url: String, handler: JsHandler[WebApi], hint: Hint, platformArg: Option[Platform] = None): Unit = {
    AMFCompiler(url, if (platformArg.isEmpty) platform else platformArg.get, hint)
      .build()
      .onComplete(callback(
        new Handler[WebApi] {
          override def error(exception: Throwable): Unit = handler.error(exception)

          override def success(document: WebApi): Unit = handler.success(document)
        },
        ""
      ))
  }

  private def callback(handler: Handler[WebApi], url: String)(t: Try[BaseUnit]) = t match {
    case Success(d: Document) => handler.success(d.encodes.asInstanceOf[WebApi])
    case Success(unit)        => handler.error(new Exception(s"Unhandled unit $unit"))
    case Failure(exception)   => handler.error(exception)
  }

  private def dumpCallback(handler: Handler[String])(t: Try[String]) = t match {
    case Success(s: String) => handler.success(s)
    case Success(unit)      => handler.error(new Exception(s"Unhandled unit $unit"))
    case Failure(exception) => handler.error(exception)
  }

  @JSExport
  def convert(stream: String, sourceHint: String, toVendor: String, handler: JsHandler[String]): Unit = {

    AMFCompiler("http://localhost.com/path", new JsTrunkPlattform(stream), matchSourceHint(sourceHint))
      .build()
      .map(bu => new AMFDumper(bu, matchToVendor(toVendor)).dump)
      .onComplete(dumpCallback(
        new Handler[String] {
          override def success(document: String): Unit = handler.success(document)

          override def error(exception: Throwable): Unit = handler.error(exception)
        }
      ))
  }

  private def matchSourceHint(source: String): Hint = {
    source match {
      case "json" | "oas" | "openapi" => OasJsonHint
      case "raml" | "yaml"            => RamlYamlHint
      case _                          => AmfJsonLdHint
    }
  }

  private def matchToVendor(toVendor: String): Vendor = {
    toVendor match {
      case "json" | "oas" | "openapi" => Oas
      case "raml" | "yaml"            => Raml
      case _                          => Amf
    }
  }

}

class JsTrunkPlattform(content: String) extends Platform {

  /** Test path resolution. */
  override def resolvePath(path: String): String = path

  /** Resolve file on specified path. */
  override protected def fetchFile(path: String): Future[Content] = {
    Future {
      Content(new CharSequenceStream(content), path)
    }
  }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    fetchFile(url)
  }

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[Unit] = ???
}

@js.native
trait JsHandler[T] extends js.Object {
  def success(document: T): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
