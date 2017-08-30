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
  * Common class for all platform generators
  */
protected abstract class PlatformGenerator extends PlatformSecrets {

  protected val target: Vendor
  protected val syntax: Syntax

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  protected def generate(unit: BaseUnit, url: String, options: GenerationOptions): Future[String] =
    AMFDumper(unit, target, syntax, options).dumpToFile(platform, url)

  protected def generate(unit: BaseUnit, options: GenerationOptions): Future[String] =
    AMFDumper(unit, target, syntax, options).dumpToString

  protected def generate(unit: BaseUnit, url: String, options: GenerationOptions, handler: Handler[Unit]): Unit = {
    generate(unit, url, options).onComplete(unitSyncAdapter(handler))
  }

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generate(unit: BaseUnit, options: GenerationOptions, handler: Handler[String]): Unit = {
    generate(unit, options).onComplete(stringSyncAdapter(handler))
  }

  private def stringSyncAdapter(handler: Handler[String])(t: Try[String]): Unit = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }

  private def unitSyncAdapter(handler: Handler[Unit])(t: Try[String]): Unit = t match {
    case Success(_)         => handler.success(Unit)
    case Failure(exception) => handler.error(exception)
  }
}
