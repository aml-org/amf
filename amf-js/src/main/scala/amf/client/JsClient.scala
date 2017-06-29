package amf.client

import amf.compiler.AMFCompiler
import amf.lexer.Token
import amf.maker.WebApiMaker
import amf.model.WebApi
import amf.parser.{AMFUnit, ASTNode, Document}
import amf.remote.{Raml, RamlYamlHint}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success, Try}

/**
  * Created by pedro.colunga on 5/19/17.
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
    val eventualAmfast = AMFCompiler(url, platform, Option(RamlYamlHint)).build()
    eventualAmfast
      .map(amfast => new WebApiMaker(AMFUnit(amfast, url, Document, Raml)).make)
      .onComplete(callbackForWebApi(
        new WebApiHandler {
          override def error(exception: Throwable): Unit = handler.error(exception)

          override def success(document: WebApi): Unit = handler.success(document)
        },
        ""
      ))
  }

  private def callback(handler: Handler, url: String)(t: Try[ASTNode[_ <: Token]]) = t match {
    case Success(value)     => handler.success(AMFUnit(value, url, Document, Raml)) //TODO hint
    case Failure(exception) => handler.error(exception)
  }

  private def callbackForWebApi(handler: WebApiHandler, url: String)(t: Try[WebApi]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }

}

@js.native
trait JsHandler extends js.Object {
  def success(document: AMFUnit): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}

@js.native
trait JsWebApiHandler extends js.Object {
  def success(document: WebApi): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
