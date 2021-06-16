package amf.apicontract.internal.spec.payload

import amf.apicontract.client.common.ProvidedMediaType

object PayloadMediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Payload,
    ProvidedMediaType.PayloadJson,
    ProvidedMediaType.PayloadYaml
  )
}
