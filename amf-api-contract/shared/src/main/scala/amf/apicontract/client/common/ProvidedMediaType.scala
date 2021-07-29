package amf.apicontract.client.common

import amf.core.internal.remote.Spec

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ProvidedMediaType")
@JSExportAll
object ProvidedMediaType {

  val Raml08: String = Spec.RAML08.mediaType + "+yaml"
  val Raml10: String = Spec.RAML10.mediaType + "+yaml"

  val Oas20: String     = Spec.OAS20.mediaType
  val Oas20Yaml: String = Spec.OAS20.mediaType + "+yaml"
  val Oas20Json: String = Spec.OAS20.mediaType + "+json"

  val Oas30: String     = Spec.OAS30.mediaType
  val Oas30Yaml: String = Spec.OAS30.mediaType + "+yaml"
  val Oas30Json: String = Spec.OAS30.mediaType + "+json"

  val Async20: String     = Spec.ASYNC20.mediaType
  val Async20Yaml: String = Spec.ASYNC20.mediaType + "+yaml"
  val Async20Json: String = Spec.ASYNC20.mediaType + "+json"

  val Payload: String     = Spec.PAYLOAD.mediaType
  val PayloadYaml: String = Spec.PAYLOAD.mediaType + "+yaml"
  val PayloadJson: String = Spec.PAYLOAD.mediaType + "+json"

  val AMF: String        = Spec.AMF.mediaType
  val JsonSchema: String = Spec.JSONSCHEMA.mediaType

}
