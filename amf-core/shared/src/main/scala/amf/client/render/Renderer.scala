package amf.client.render

import java.io.File

import amf.client.convert.CoreClientConverters._
import amf.client.model.document.BaseUnit
import amf.core.AMFSerializer
import amf.core.client.Handler
import amf.core.model.document.{BaseUnit => InternalBaseUnit}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success, Try}

/**
  * Base class for a renderer.
  */
class Renderer(vendor: String, mediaType: String) {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFile(unit: BaseUnit, url: String, handler: FileHandler): Unit =
    generateFile(unit, url, RenderOptions(), handler)

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: RenderOptions, handler: FileHandler): Unit =
    generate(unit._internal, url, options, UnitHandlerAdapter(handler))

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, handler: Handler[String]): Unit =
    generateString(unit, RenderOptions(), handler)

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, options: RenderOptions, handler: Handler[String]): Unit =
    generate(unit._internal, options, StringHandlerAdapter(handler))

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFile(unit: BaseUnit, url: String): ClientFuture[Unit] = generateFile(unit, url, RenderOptions())

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: RenderOptions): ClientFuture[Unit] =
    generate(unit._internal, url, RenderOptions()).asClient

  /** Asynchronously renders the syntax text and returns it. */
  @JSExport
  def generateString(unit: BaseUnit): ClientFuture[String] = generateString(unit, RenderOptions())

  /** Asynchronously renders the syntax text and returns it. */
  @JSExport
  def generateString(unit: BaseUnit, options: RenderOptions): ClientFuture[String] =
    generate(unit._internal, options).asClient

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, output: File, handler: FileHandler): Unit =
    generateFile(unit, output, RenderOptions(), handler)

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, output: File, options: RenderOptions, handler: FileHandler): Unit =
    generateFile(unit, "file://" + output.getAbsolutePath, options, handler)

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, output: File): ClientFuture[Unit] = generateFile(unit, output, RenderOptions())

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, output: File, options: RenderOptions): ClientFuture[Unit] = {
    generateFile(unit, "file://" + output.getAbsolutePath, options)
  }

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  private def generate(unit: InternalBaseUnit, url: String, options: RenderOptions): Future[Unit] =
    AMFSerializer(unit, mediaType, vendor, options).renderToFile(platform, url)

  private def generate(unit: InternalBaseUnit, options: RenderOptions): Future[String] =
    AMFSerializer(unit, mediaType, vendor, options).renderToString

  private def generate(unit: InternalBaseUnit, url: String, options: RenderOptions, handler: Handler[Unit]): Unit = {
    generate(unit, url, options).onComplete(unitSyncAdapter(handler))
  }

  /** Generates the syntax text and returns it to the provided callback. */
  private def generate(unit: InternalBaseUnit, options: RenderOptions, handler: Handler[String]): Unit = {
    generate(unit, options).onComplete(stringSyncAdapter(handler))
  }

  private def stringSyncAdapter(handler: Handler[String])(t: Try[String]): Unit = t match {
    case Success(value)     => handler.success(value)
    case Failure(exception) => handler.error(exception)
  }

  private def unitSyncAdapter(handler: Handler[Unit])(t: Try[Unit]): Unit = t match {
    case Success(unit)      => handler.success(unit)
    case Failure(exception) => handler.error(exception)
  }

  private case class UnitHandlerAdapter(handler: FileHandler) extends Handler[Unit] {
    override def success(unit: Unit): Unit         = handler.success()
    override def error(exception: Throwable): Unit = handler.error(exception)
  }

  private case class StringHandlerAdapter(handler: Handler[String]) extends Handler[String] {
    override def success(document: String): Unit   = handler.success(document)
    override def error(exception: Throwable): Unit = handler.error(exception)
  }
}
