package amf.client.render

import java.io.File

import amf.client.convert.CoreClientConverters._
import amf.client.handler.{FileHandler, Handler}
import amf.client.model.document.BaseUnit
import amf.core.AMFSerializer
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
  def generateFile(unit: BaseUnit, url: String, handler: ClientFileHandler): Unit =
    generateFile(unit, url, RenderOptions(), handler)

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: RenderOptions, handler: ClientFileHandler): Unit =
    generate(unit._internal, url, options, handler)

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, handler: ClientResultHandler[String]): Unit =
    generateString(unit, RenderOptions(), handler)

  /** Generates the syntax text and returns it to the provided callback. */
  @JSExport
  def generateString(unit: BaseUnit, options: RenderOptions, handler: ClientResultHandler[String]): Unit =
    generate(unit._internal, options, handler)

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
  def generateFile(unit: BaseUnit, output: File, handler: ClientFileHandler): Unit =
    generateFile(unit, output, RenderOptions(), handler)

  /**
    * Asynchronously renders the syntax text and stores it in the file pointed by the provided URL.
    * It must throw an UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateFile(unit: BaseUnit, output: File, options: RenderOptions, handler: ClientFileHandler): Unit =
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

  private def generate(unit: InternalBaseUnit, url: String, options: RenderOptions, handler: FileHandler): Unit = {
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

  private def unitSyncAdapter(handler: FileHandler)(t: Try[Unit]): Unit = t match {
    case Success(_)         => handler.success()
    case Failure(exception) => handler.error(exception)
  }
}
