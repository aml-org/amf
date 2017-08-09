package amf.client

import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  *
  */
abstract class BaseGenerator extends PlatformSecrets {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  protected def generateFile(unit: BaseUnit, url: String, syntax: Vendor, handler: FileHandler): Unit = {
    AMFDumper(unit, syntax)
      .dumpToFile(platform, url)
      .onComplete(dumpCallback(new Handler[String] {
        override def error(exception: Throwable): Unit = handler.error(exception)

        override def success(document: String): Unit = handler.success()
      }, ""))

  }

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generateString(unit: BaseUnit, syntax: Vendor, handler: StringHandler): Unit = {
    AMFDumper(unit, syntax).dump.onComplete(
      dumpCallback(
        new Handler[String] {
          override def error(exception: Throwable): Unit = handler.error(exception)

          override def success(document: String): Unit = handler.success(document)
        },
        ""
      ))
  }

  private def dumpCallback(handler: Handler[String], url: String)(t: Try[String]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
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

trait Generator[T] {
  def generateToFile(unit: T, url: String, syntax: Vendor, handler: FileHandler): Unit

  def generateToString(unit: T, syntax: Vendor, handler: StringHandler): Unit
}
