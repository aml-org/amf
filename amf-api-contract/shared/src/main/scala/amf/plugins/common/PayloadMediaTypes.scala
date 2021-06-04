package amf.plugins.common

import amf.core.remote.Payload

object PayloadMediaTypes {

  val mediaTypes = Seq(
    Payload.mediaType,
    "application/payload+json",
    "application/payload+yaml"
  )
}
