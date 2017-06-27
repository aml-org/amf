package amf.client

import amf.parser.Container

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

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
}

@js.native
trait JsHandler extends js.Object {
  def success(document: Container): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
