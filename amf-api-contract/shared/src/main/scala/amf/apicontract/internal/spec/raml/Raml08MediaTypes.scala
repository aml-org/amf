package amf.apicontract.internal.spec.raml

import amf.apicontract.client.common.ProvidedMediaType
import amf.core.internal.remote.SpecId

object Raml08MediaTypes {

  val mediaTypes = Seq(
    SpecId.RAML08.mediaType,
    ProvidedMediaType.Raml08 // defines yaml syntax explicitly
  )
}
