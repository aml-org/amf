package amf.client

import amf.generator.{JsonGenerator, YamlGenerator}
import amf.parser.AMFUnit

import scala.util.{Failure, Success, Try}

/**
  * Created by pedro.colunga on 5/29/17.
  */
abstract class BaseGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  protected def generateFile(document: AMFUnit, url: String, syntax: String, handler: FileHandler): Unit = {}

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generateString(document: AMFUnit, syntax: String, handler: StringHandler): Unit = {
    Try {
      syntax match {
        case "json" => new JsonGenerator().generate(document.root)
        case "yaml" => new YamlGenerator().generate(document.root)
      }
    } match {
      case Success(writer)    => handler.success(writer.toString)
      case Failure(exception) => handler.error(exception)
    }
  }
}

trait StringHandler {
  def success(generation: String)
  def error(exception: Throwable)
}

trait FileHandler {
  def error(exception: Throwable)
}
