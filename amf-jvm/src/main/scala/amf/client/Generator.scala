package amf.client

import java.util.concurrent.CompletableFuture

import amf.client.FutureConverter.converters
import amf.model.BaseUnit
import amf.remote.Vendor

import scala.language.implicitConversions

/**
  *
  */
class Generator extends BaseGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, url: String, syntax: Vendor, handler: FileHandler): Unit =
    super.generateAndHanldeFile(unit.unit, url, syntax, jvmFileHander(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  def generateString(unit: BaseUnit, syntax: Vendor, handler: StringHandler): Unit =
    super.generateAndHandleString(unit.unit, syntax, jvmStringHander(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFileAsync(unit: BaseUnit, url: String, syntax: Vendor): CompletableFuture[String] =
    super.generateFile(unit.unit, url, syntax).asJava

  /** Generates the syntax text and returns it  asynchronously. */
  def generateStringAsync(unit: BaseUnit, syntax: Vendor): CompletableFuture[String] =
    super.generateString(unit.unit, syntax).asJava

  private def jvmFileHander(handler: FileHandler) =
    new Handler[Unit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: Unit): Unit = handler.success()
    }

  private def jvmStringHander(handler: StringHandler) =
    new Handler[String] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: String): Unit = handler.success(document)
    }
}

trait StringHandler {
  def success(generation: String)
  def error(exception: Throwable)
}

trait FileHandler {
  def error(exception: Throwable)
  def success()
}
