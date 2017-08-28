package amf.client

import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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
  protected def generateFile(unit: BaseUnit, url: String, syntax: Vendor): Future[String] =
    AMFDumper(unit, syntax).dumpToFile(platform, url)

  protected def generateAndHanldeFile(unit: BaseUnit, url: String, syntax: Vendor, handler: Handler[Unit]): Unit = {
    generateFile(unit, url, syntax)
      .onComplete(callback(new Handler[String] {
        override def error(exception: Throwable): Unit = handler.error(exception)
        override def success(document: String): Unit   = handler.success()
      }, ""))
  }

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generateAndHandleString(unit: BaseUnit, syntax: Vendor, handler: Handler[String]): Unit = {
    generateString(unit, syntax).onComplete(callback(handler, ""))
  }

  protected def generateString(unit: BaseUnit, syntax: Vendor): Future[String] = AMFDumper(unit, syntax).dumpToStream

  private def callback[T](handler: Handler[String], url: String)(t: Try[String]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }
}
