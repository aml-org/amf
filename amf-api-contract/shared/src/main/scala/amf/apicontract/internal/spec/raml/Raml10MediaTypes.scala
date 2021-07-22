package amf.apicontract.internal.spec.raml

import amf.core.internal.remote.Vendor

object Raml10MediaTypes {

  val mediaTypes = Seq(
    Vendor.RAML10.mediaType,
    "application/raml10+yaml" // defines yaml syntax explicitly
  )
}
