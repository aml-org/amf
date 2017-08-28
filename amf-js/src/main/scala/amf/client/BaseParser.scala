package amf.client

import amf.model.{BaseUnit, Document}
import amf.remote.Syntax.Syntax
import amf.remote._
import amf.unsafe.TrunkPlatform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Base class for js parsers
  */
class BaseParser(protected val vendor: Vendor, protected val syntax: Syntax) extends PlatformParser {

  /**
    * generates the base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  @JSExport
  def parseFile(url: String, handler: JsHandler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler))

  /**
    * generates the base unit document from given stream (api)
    * @param stream: the api stream
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  @JSExport
  def parseString(stream: String, handler: JsHandler[BaseUnit]): Unit =
    super.parse(null, BaseUnitHandlerAdapter(handler), Some(TrunkPlatform(stream)))

  /**
    * generates asynchronously base unit document from api located in the given file or url.
    * @param url : location of the api
    */
  @JSExport
  def parseFileAsync(url: String): js.Promise[BaseUnit] =
    super.parseAsync(url).map(bu => Document(bu)).toJSPromise

  /**
    * generates asynchronously base unit document from given stream (api)
    * @param stream: the api stream
    */
  @JSExport
  def parseStringAsync(stream: String): js.Promise[BaseUnit] =
    super.parseAsync(null, Some(TrunkPlatform(stream))).map(bu => Document(bu)).toJSPromise

  private case class BaseUnitHandlerAdapter(handler: JsHandler[BaseUnit]) extends Handler[amf.document.BaseUnit] {
    override def success(document: amf.document.BaseUnit): Unit = handler.success(Document(document))
    override def error(exception: Throwable): Unit              = handler.error(exception)
  }
}

@js.native
trait JsHandler[T] extends js.Object {
  def success(document: T): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
