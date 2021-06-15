package amf.plugins.common

import amf.client.exported.ProvidedMediaType

object Oas20MediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Oas20Json,
    ProvidedMediaType.Oas20Yaml,
    ProvidedMediaType.Oas20,
    "application/swagger+json",
    "application/swagger20+json",
    "application/swagger+yaml",
    "application/swagger20+yaml",
    "application/swagger",
    "application/swagger20"
  )
}
