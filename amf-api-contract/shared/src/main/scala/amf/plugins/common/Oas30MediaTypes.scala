package amf.plugins.common

import amf.client.exported.ProvidedMediaType

object Oas30MediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Oas30Json,
    ProvidedMediaType.Oas30Yaml,
    ProvidedMediaType.Oas30,
    "application/openapi30",
    "application/openapi30+yaml",
    "application/openapi30+json"
  )
}
