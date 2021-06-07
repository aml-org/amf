package amf.plugins.common

import amf.core.internal.remote.Payload
import amf.client.exported.ProvidedMediaType
object PayloadMediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Payload,
    ProvidedMediaType.PayloadJson,
    ProvidedMediaType.PayloadYaml
  )
}
