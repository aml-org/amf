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
@JSExportTopLevel("Generator")
class Generator extends BaseGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFile(unit: BaseUnit, url: String, vendor: Vendor, handler: FileHandler): Unit =
    super.generateAndHanldeFile(unit.unit, url, vendor, jsUnitHander(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, vendor: Vendor, handler: StringHandler): Unit =
    super.generateAndHandleString(unit.unit, vendor, jsStringHander(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFileAsync(unit: BaseUnit, url: String, vendor: Vendor): js.Promise[String] =
    super.generateFile(unit.unit, url, vendor).toJSPromise

  /** Generates the syntax text and returns it  asynchronously. */
  @JSExport
  def generateStringAsync(unit: BaseUnit, vendor: Vendor): js.Promise[String] =
    super.generateString(unit.unit, vendor).toJSPromise

  private def jsStringHander(handler: StringHandler) =
    new Handler[String] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: String): Unit =
        handler.success(document)
    }

  private def jsUnitHander(handler: FileHandler) =
    new Handler[Unit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: Unit): Unit =
        handler.success()
    }
}

@js.native
trait StringHandler extends JsHandler[String] {
  def success(generation: String)
  def error(exception: Throwable)
}

@js.native
trait FileHandler extends JsHandler[Unit] {
  def error(exception: Throwable)
  def success()
}
