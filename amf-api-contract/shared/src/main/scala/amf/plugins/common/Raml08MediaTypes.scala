package amf.plugins.common

import amf.client.exported.ProvidedMediaType
import amf.core.internal.remote.Vendor

object Raml08MediaTypes {

  val mediaTypes = Seq(
    Vendor.RAML08.mediaType,
    ProvidedMediaType.Raml08 // defines yaml syntax explicitly
  )
}
