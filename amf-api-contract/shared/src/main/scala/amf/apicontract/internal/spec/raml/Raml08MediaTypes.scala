package amf.apicontract.internal.spec.raml

import amf.apicontract.client.common.ProvidedMediaType
import amf.core.internal.remote.Spec

object Raml08MediaTypes {

  val mediaTypes = Seq(
    Spec.RAML08.mediaType,
    ProvidedMediaType.Raml08 // defines yaml syntax explicitly
  )
}
