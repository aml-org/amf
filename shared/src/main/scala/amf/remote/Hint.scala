package amf.remote

import amf.framework.parser.{ReferenceKind, Unspecified}
import amf.remote.Syntax.{Json, Syntax, Yaml}

case class Hint(vendor: Vendor, syntax: Syntax, kind: ReferenceKind = Unspecified) {
  def +(k: ReferenceKind): Hint = copy(kind = k)
}

object RamlYamlHint extends Hint(Raml, Yaml)

object RamlJsonHint extends Hint(Raml, Json)

object OasYamlHint extends Hint(Oas, Yaml)

object OasJsonHint extends Hint(Oas, Json)

object AmfJsonHint extends Hint(Amf, Json)

object PayloadJsonHint extends Hint(Payload, Json)

object PayloadYamlHint extends Hint(Payload, Yaml)
