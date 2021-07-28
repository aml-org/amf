package amf.apicontract.internal.spec.raml

import amf.core.internal.remote.SpecId

object Raml10MediaTypes {

  val mediaTypes = Seq(
    SpecId.RAML10.mediaType,
    "application/raml10+yaml" // defines yaml syntax explicitly
  )
}
