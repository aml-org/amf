package amf.testing

import amf.core.internal.remote._
import amf.testing.MediaType._

case class Target(spec: Vendor, mediaType: String)

object AmfJsonLd extends Target(Amf, `application/ld+json`)

object Raml08Yaml extends Target(Raml08, `application/yaml`)
object Raml10Yaml extends Target(Raml10, `application/yaml`)

object Oas20Yaml extends Target(Oas20, `application/yaml`)
object Oas20Json extends Target(Oas20, `application/json`)

object Oas30Yaml extends Target(Oas30, `application/yaml`)
object Oas30Json extends Target(Oas30, `application/json`)

object Async20Yaml extends Target(AsyncApi20, `application/yaml`)
object Async20Json extends Target(AsyncApi20, `application/json`)

object HintProvider {

  def defaultHintFor(vendor: Vendor): Hint = vendor match {
    case Amf        => AmfJsonHint
    case Raml08     => Raml08YamlHint
    case Raml10     => Raml10YamlHint
    case Oas20      => Oas20JsonHint
    case Oas30      => Oas30JsonHint
    case AsyncApi20 => Async20YamlHint
  }
}
