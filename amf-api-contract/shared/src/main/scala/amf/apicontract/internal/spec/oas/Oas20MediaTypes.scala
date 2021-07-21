package amf.apicontract.internal.spec.oas

import amf.apicontract.client.common.ProvidedMediaType

object Oas20MediaTypes {

  val mediaTypes = Seq(
    ProvidedMediaType.Oas20Json,
    ProvidedMediaType.Oas20Yaml,
    "application/oas20",
    "application/swagger+json",
    "application/swagger20+json",
    "application/swagger+yaml",
    "application/swagger20+yaml",
    "application/swagger",
    "application/swagger20"
  )
}
