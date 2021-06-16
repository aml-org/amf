package amf.apicontract.internal.spec.payload

import amf.apicontract.client.platform.ProvidedMediaType

object PayloadMediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Payload,
    ProvidedMediaType.PayloadJson,
    ProvidedMediaType.PayloadYaml
  )
}
