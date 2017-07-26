package amf.client

import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.{Oas, Raml}

import scala.util.{Failure, Success, Try}

/**
  *
  */
abstract class BaseGenerator {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  protected def generateFile(unit: BaseUnit, url: String, syntax: String, handler: FileHandler): Unit = {}

  /** Generates the syntax text and returns it to the provided callback. */
  protected def generateString(unit: BaseUnit, syntax: String, handler: StringHandler): Unit = {
    Try {
      syntax match {
        case "json" => new AMFDumper(unit, Oas).dump
        case "yaml" => new AMFDumper(unit, Raml).dump
      }
    } match {
      case Success(content)   => handler.success(content)
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
