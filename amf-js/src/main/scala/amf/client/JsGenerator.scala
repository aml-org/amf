package amf.client
import amf.model.BaseUnit
import amf.remote.Vendor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  *
  */
@JSExportTopLevel("JsGenerator")
class JsGenerator extends BaseGenerator with Generator[BaseUnit] {

  override type FH = JsFileHandler

  override type SH = JsStringHandler

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  override def generateToFile(unit: BaseUnit, url: String, vendor: Vendor, handler: JsFileHandler): Unit =
    super.generateAndHanldeFile(unit.unit, url, vendor, jsUnitHander(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  override def generateToString(unit: BaseUnit, vendor: Vendor, handler: JsStringHandler): Unit =
    super.generateAndHandleString(unit.unit, vendor, jsStringHander(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateToFileAsync(unit: BaseUnit, url: String, vendor: Vendor): js.Promise[String] =
    super.generateFile(unit.unit, url, vendor).toJSPromise

  /** Generates the syntax text and returns it  asynchronously. */
  @JSExport
  def generateToStringAsync(unit: BaseUnit, vendor: Vendor): js.Promise[String] =
    super.generateString(unit.unit, vendor).toJSPromise

  private def jsStringHander(handler: JsHandler[String]) =
    new Handler[String] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: String): Unit =
        handler.success(document)
    }

  private def jsUnitHander(handler: JsHandler[Unit]) =
    new Handler[Unit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: Unit): Unit =
        handler.success()
    }
}

@js.native
trait JsStringHandler extends JsHandler[String] {
  def success(generation: String)
  def error(exception: Throwable)
}

@js.native
trait JsFileHandler extends JsHandler[Unit] {
  def error(exception: Throwable)
  def success()
}
