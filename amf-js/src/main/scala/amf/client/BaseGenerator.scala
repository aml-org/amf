package amf.client
import amf.model.BaseUnit
import amf.remote.Syntax.Syntax
import amf.remote.Vendor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Base class for JS generators.
  */
protected abstract class BaseGenerator(protected val target: Vendor, protected val syntax: Syntax)
    extends PlatformGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFile(unit: BaseUnit, url: String, handler: FileHandler): Unit =
    generateSync(unit.unit, url, GenerationOptions(), UnitHandlerAdapter(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, handler: StringHandler): Unit =
    generateSync(unit.unit, GenerationOptions(), StringHandlerAdapter(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFileAsync(unit: BaseUnit, url: String): js.Promise[String] =
    generateAsync(unit.unit, url, GenerationOptions()).toJSPromise

  /** Generates the syntax text and returns it asynchronously. */
  @JSExport
  def generateStringAsync(unit: BaseUnit): js.Promise[String] =
    generateAsync(unit.unit, GenerationOptions()).toJSPromise

  protected case class UnitHandlerAdapter(handler: FileHandler) extends Handler[Unit] {
    override def success(unit: Unit): Unit         = handler.success()
    override def error(exception: Throwable): Unit = handler.error(exception)
  }

  protected case class StringHandlerAdapter(handler: StringHandler) extends Handler[String] {
    override def success(document: String): Unit   = handler.success(document)
    override def error(exception: Throwable): Unit = handler.error(exception)
  }
}

/** Interface that needs to be implemented to handle a string result, or an exception if something went wrong. */
@js.native
trait StringHandler extends JsHandler[String] {
  def success(generation: String)
  def error(exception: Throwable)
}

/** Interface that needs to be implemented to handle a success result from writing a file, or an exception if something went wrong. */
@js.native
trait FileHandler extends JsHandler[Unit] {
  def error(exception: Throwable)
  def success()
}
