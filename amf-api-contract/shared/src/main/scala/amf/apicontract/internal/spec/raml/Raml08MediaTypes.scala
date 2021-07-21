package amf.apicontract.internal.spec.raml

import amf.apicontract.client.common.ProvidedMediaType
import amf.core.internal.remote.Vendor

object Raml08MediaTypes {

  val mediaTypes = Seq(
    "application/raml08",
    ProvidedMediaType.Raml08 // defines yaml syntax explicitly
  )
}
