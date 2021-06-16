package amf.apicontract.client.common

import amf.core.internal.remote.Vendor

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ProvidedMediaType")
@JSExportAll
object ProvidedMediaType {

  val Raml08: String = Vendor.RAML08.mediaType + "+yaml"
  val Raml10: String = Vendor.RAML10.mediaType + "+yaml"

  val Oas20: String     = Vendor.OAS20.mediaType
  val Oas20Yaml: String = Vendor.OAS20.mediaType + "+yaml"
  val Oas20Json: String = Vendor.OAS20.mediaType + "+json"

  val Oas30: String     = Vendor.OAS30.mediaType
  val Oas30Yaml: String = Vendor.OAS30.mediaType + "+yaml"
  val Oas30Json: String = Vendor.OAS30.mediaType + "+json"

  val Async20: String     = Vendor.ASYNC20.mediaType
  val Async20Yaml: String = Vendor.ASYNC20.mediaType + "+yaml"
  val Async20Json: String = Vendor.ASYNC20.mediaType + "+json"

  val Payload: String     = Vendor.PAYLOAD.mediaType
  val PayloadYaml: String = Vendor.PAYLOAD.mediaType + "+yaml"
  val PayloadJson: String = Vendor.PAYLOAD.mediaType + "+json"

  val AMF: String        = Vendor.AMF.mediaType
  val JsonSchema: String = Vendor.JSONSCHEMA.mediaType

}
