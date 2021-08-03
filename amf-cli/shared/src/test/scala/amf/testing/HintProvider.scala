package amf.testing

import amf.core.internal.remote._

object HintProvider {

  def defaultHintFor(spec: Spec): Hint = spec match {
    case Amf        => AmfJsonHint
    case Raml08     => Raml08YamlHint
    case Raml10     => Raml10YamlHint
    case Oas20      => Oas20JsonHint
    case Oas30      => Oas30JsonHint
    case AsyncApi20 => Async20YamlHint
  }
}
