package amf.client

import amf.compiler.AMFCompiler
import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.remote.RamlYamlHint

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success, Try}

/**
  *
  */
@JSExportTopLevel("JsClient")
class JsClient extends BaseClient {
  @JSExport
  def generate(url: String, syntax: String, handler: JsHandler): Unit = {
    /*generate(url, syntax, new Handler {
            override def success(document: Document): Unit = handler.success(document)
            override def error(exception: Throwable): Unit = handler.error(exception)
        })*/
  }

  @JSExport
  def webApiClass(url: String, handler: JsWebApiHandler): Unit = {
    AMFCompiler(url, platform, RamlYamlHint)
      .build()
      .onComplete(callback(
        new WebApiHandler {
          override def error(exception: Throwable): Unit = handler.error(exception)

          override def success(document: WebApi): Unit = handler.success(document)
        },
        ""
      ))
  }

  private def callback(handler: WebApiHandler, url: String)(t: Try[BaseUnit]) = t match {
    case Success(value) =>
      value match {
        case d: Document if d.encodes.isInstanceOf[WebApi] =>
          handler.success(d.encodes.asInstanceOf[WebApi])
        case _ => handler.error(new Exception(s"Unhandled type $value"))
      }
    case Failure(exception) => handler.error(exception)
  }

}

@js.native
trait JsHandler extends js.Object {
  def success(document: BaseUnit): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}

@js.native
trait JsWebApiHandler extends js.Object {
  def success(document: WebApi): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
