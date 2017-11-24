package amf.emit

import amf.core.AMFSerializer
import amf.core.client.GenerationOptions
import amf.framework.model.document.BaseUnit
import amf.framework.remote._
import amf.remote._
import org.yaml.model.YDocument

/**
  * AMF Unit Maker
  */
// TODO: this is only here for compatibility with the test suite
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument = {
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

    new AMFSerializer(unit, mediaType, vendorString, options).make()
  }
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument =
    new AMFUnitMaker().make(unit, vendor, options)
}
