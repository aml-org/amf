package amf.client

import java.util.concurrent.CompletableFuture

import amf.remote.FutureConverter.converters
import amf.model.BaseUnit
import amf.remote.Syntax.Syntax
import amf.remote.Vendor

import scala.language.implicitConversions

/**
  * Base class for jvm generators
  */
private[client] abstract class BaseGenerator(protected val target: Vendor, protected val syntax: Syntax)
    extends PlatformGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, url: String, handler: FileHandler): Unit =
    super.generateFile(unit.unit, url, UnitHandlerAdapter(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  def generateString(unit: BaseUnit, handler: StringHandler): Unit =
    super.generateString(unit.unit, StringHandlerAdapter(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFileAsync(unit: BaseUnit, url: String): CompletableFuture[String] =
    super.generateFileAsync(unit.unit, url).asJava

  /** Generates the syntax text and returns it  asynchronously. */
  def generateStringAsync(unit: BaseUnit): CompletableFuture[String] =
    super.generateStringAsync(unit.unit).asJava

  private case class UnitHandlerAdapter(handler: FileHandler) extends Handler[Unit] {
    override def success(unit: Unit): Unit         = handler.success()
    override def error(exception: Throwable): Unit = handler.error(exception)
  }

  private case class StringHandlerAdapter(handler: StringHandler) extends Handler[String] {
    override def success(document: String): Unit   = handler.success(document)
    override def error(exception: Throwable): Unit = handler.error(exception)
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
