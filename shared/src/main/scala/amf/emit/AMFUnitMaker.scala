package amf.emit

import amf.client.GenerationOptions
import amf.core.AMFSerializer
import amf.document.BaseUnit
import amf.remote._
import org.yaml.model.YDocument

/**
  * AMF Unit Maker
  */
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

    new AMFSerializer().make(unit, mediaType, vendorString, options)
  }
  /*
  def make(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument = {
    vendor match {
      case Amf | Payload => makeAmfWebApi(unit, options)
      case Raml | Oas    => makeUnitWithSpec(unit, vendor)
      case Unknown       => throw new Exception("Cannot make unit for unknown provider")
    }
  }
  private def isDialect(unit: BaseUnit) = new RAMLExtensionsPlugin().canUnparse(unit)

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): YDocument = {
    vendor match {
      case Raml if isDialect(unit) => makeRamlDialect(unit)
      case Raml                    => makeRamlUnit(unit)
      case Oas                     => makeOasUnit(unit)
      case _                       => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeRamlDialect(unit: BaseUnit): YDocument = new RAMLExtensionsPlugin().unparse(unit, GenerationOptions()).get

  private def makeRamlUnit(unit: BaseUnit): YDocument = {
    val plugin = new RAML10Plugin()
    if (plugin.canUnparse(unit)) {
      plugin.unparse(unit, GenerationOptions()).get
    } else {
      throw new Exception("Cannot accept supposed to be RAML 1.0 unit")
    }
  }

  private def makeOasUnit(unit: BaseUnit): YDocument = {
    val plugin = new OAS20Plugin()
    if (plugin.canUnparse(unit)) {
      plugin.unparse(unit, GenerationOptions()).get
    } else {
      throw new Exception("Cannot accept supposed to be OAS 2.0 unit")
    }
  }

  private def makeAmfWebApi(unit: BaseUnit, options: GenerationOptions): YDocument =
    new AMFGraphPlugin().unparse(unit, options).get
  */
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument =
    new AMFUnitMaker().make(unit, vendor, options)
}
