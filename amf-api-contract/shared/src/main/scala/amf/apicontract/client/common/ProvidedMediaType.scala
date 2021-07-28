package amf.apicontract.client.common

import amf.core.internal.remote.SpecId

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ProvidedMediaType")
@JSExportAll
object ProvidedMediaType {

  val Raml08: String = SpecId.RAML08.mediaType + "+yaml"
  val Raml10: String = SpecId.RAML10.mediaType + "+yaml"

  val Oas20: String     = SpecId.OAS20.mediaType
  val Oas20Yaml: String = SpecId.OAS20.mediaType + "+yaml"
  val Oas20Json: String = SpecId.OAS20.mediaType + "+json"

  val Oas30: String     = SpecId.OAS30.mediaType
  val Oas30Yaml: String = SpecId.OAS30.mediaType + "+yaml"
  val Oas30Json: String = SpecId.OAS30.mediaType + "+json"

  val Async20: String     = SpecId.ASYNC20.mediaType
  val Async20Yaml: String = SpecId.ASYNC20.mediaType + "+yaml"
  val Async20Json: String = SpecId.ASYNC20.mediaType + "+json"

  val Payload: String     = SpecId.PAYLOAD.mediaType
  val PayloadYaml: String = SpecId.PAYLOAD.mediaType + "+yaml"
  val PayloadJson: String = SpecId.PAYLOAD.mediaType + "+json"

  val AMF: String        = SpecId.AMF.mediaType
  val JsonSchema: String = SpecId.JSONSCHEMA.mediaType

}
