package amf.core.client

import java.io.File
import java.util.concurrent.CompletableFuture

import amf.core.remote.FutureConverter._
import amf.model.document.BaseUnit

import scala.language.implicitConversions

/**
  * Base class for JVM generators.
  */
class Generator(protected val vendor: String, protected val mediaType: String) extends PlatformGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, path: File, handler: FileHandler): Unit =
    generate(unit.element, "file://" + path.getAbsolutePath, GenerationOptions(), UnitHandlerAdapter(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  def generateString(unit: BaseUnit, handler: StringHandler): Unit =
    generate(unit.element, GenerationOptions(), StringHandlerAdapter(handler))

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, url: File): CompletableFuture[String] =
    generate(unit.element, "file://" + url.getAbsolutePath, GenerationOptions()).asJava

  /** Generates the syntax text and returns it. */
  def generateString(unit: BaseUnit): String = generate(unit.element, GenerationOptions())

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
trait StringHandler {
  def success(generation: String)
  def error(exception: Throwable)
}

/** Interface that needs to be implemented to handle a success result from writing a file, or an exception if something went wrong. */
trait FileHandler {
  def error(exception: Throwable)
  def success()
}
