package amf.apicontract.internal.spec.raml

import amf.core.internal.remote.Spec

object Raml10MediaTypes {

  val mediaTypes = Seq(
    Spec.RAML10.mediaType,
    "application/raml10+yaml" // defines yaml syntax explicitly
  )
}
