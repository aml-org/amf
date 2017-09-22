package amf.client

import amf.model.{BaseUnit, Document, Module}
import amf.remote.Syntax.Syntax
import amf.remote._
import amf.unsafe.TrunkPlatform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Base class for JS parsers.
  */
class BaseParser(protected val vendor: Vendor, protected val syntax: Syntax) extends PlatformParser {

  private def unitScalaToJVM(unit: amf.document.BaseUnit): BaseUnit = unit match {
    case d: amf.document.Document => Document(d)
    case m: amf.document.Module   => Module(m)
  }

  /**
    * Generates a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  @JSExport
  def parseFile(url: String, handler: JsHandler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler))

  /**
    * Generates the [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  @JSExport
  def parseString(stream: String, handler: JsHandler[BaseUnit]): Unit =
    super.parse(null, BaseUnitHandlerAdapter(handler), Some(TrunkPlatform(stream)))

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @return A js promise that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  @JSExport
  def parseFileAsync(url: String): js.Promise[BaseUnit] =
    super.parseAsync(url).map(unitScalaToJVM).toJSPromise

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string
    * @return A js promise that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  @JSExport
  def parseStringAsync(stream: String): js.Promise[BaseUnit] =
    super.parseAsync(null, Some(TrunkPlatform(stream))).map(unitScalaToJVM).toJSPromise

  private case class BaseUnitHandlerAdapter(handler: JsHandler[BaseUnit]) extends Handler[amf.document.BaseUnit] {
    override def success(document: amf.document.BaseUnit): Unit = handler.success(unitScalaToJVM(document))
    override def error(exception: Throwable): Unit              = handler.error(exception)
  }
}

/** Trait that needs to be implemented to handle different kinds of results. */
@js.native
trait JsHandler[T] extends js.Object {
  def success(document: T): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
