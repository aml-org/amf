package amf.dumper

import amf.core.AMFSerializer
import amf.core.client.GenerationOptions
import amf.framework.model.document.BaseUnit
import amf.framework.remote._
import amf.framework.remote.Syntax.Syntax
import amf.remote._

import scala.concurrent.Future

// TODO: this is only here for compatibility with the test suite
class AMFDumper(unit: BaseUnit, vendor: Vendor, syntax: Syntax, options: GenerationOptions) {

  /** Print ast to string. */
  def dumpToString: String = dump()

  /** Print ast to file. */
  def dumpToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump())

  private def dump(): String = {
    val vendorString = vendor match {
      case Amf           => "AMF Graph"
      case Payload       => "AMF Payload"
      case Raml          => "RAML 1.0"
      case Oas           => "OAS 2.0"
      case Extension     => "RAML Extension"
      case Unknown       => "Uknown Vendor"
    }

    val mediaType = vendor match {
      case Amf           => "application/ld+json"
      case Payload       => "application/amf+json"
      case Raml          => "application/yaml"
      case Oas           => "application/json"
      case Extension     => "application/yaml"
      case Unknown       => "text/plain"
    }

    new AMFSerializer(unit, mediaType, vendorString, options).dumpToString
  }

  private def unsupported = {
    throw new RuntimeException(s"Unsupported '$syntax' syntax for '$vendor'")
  }
}

object AMFDumper {
  def apply(unit: BaseUnit, vendor: Vendor, syntax: Syntax, options: GenerationOptions): AMFDumper =
    new AMFDumper(unit, vendor, syntax, options)
}
