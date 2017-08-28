package amf.client

import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.Syntax.Syntax
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  *
  */
private[client] abstract class PlatformGenerator extends PlatformSecrets {

  protected val target: Vendor
  protected val syntax: Syntax

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  protected def generateFileAsync(unit: BaseUnit, url: String): Future[String] =
    AMFDumper(unit, target, syntax).dumpToFile(platform, url)

  protected def generateStringAsync(unit: BaseUnit): Future[String] =
    AMFDumper(unit, target, syntax).dumpToString

  protected def generateFile(unit: BaseUnit, url: String, handler: Handler[Unit]): Unit = {
    generateFileAsync(unit, url).onComplete(unitCallbackAdapter(handler))
  }

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generateString(unit: BaseUnit, handler: Handler[String]): Unit = {
    generateStringAsync(unit).onComplete(stringCallbackAdapter(handler))
  }

  private def stringCallbackAdapter(handler: Handler[String])(t: Try[String]) = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }

  private def unitCallbackAdapter(handler: Handler[Unit])(t: Try[String]) = t match {
    case Success(value)     => handler.success()
    case Failure(exception) => handler.error(exception)
  }
}
