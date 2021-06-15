package amf.plugins.common

import amf.client.exported.ProvidedMediaType
import amf.core.internal.remote.Vendor

object Raml10MediaTypes {

  val mediaTypes = Seq(
    Vendor.RAML10.mediaType,
    ProvidedMediaType.Raml10 // defines yaml syntax explicitly
  )
}
