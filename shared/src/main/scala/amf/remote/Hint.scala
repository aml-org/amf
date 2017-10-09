package amf.remote

import amf.remote.Syntax.{Json, Syntax, Yaml}

case class Hint(vendor: Vendor, syntax: Syntax, kind: Kind = Unspecified) {
  def +(k: Kind): Hint = copy(kind = k)
}

object RamlYamlHint extends Hint(Raml, Yaml)

object RamlJsonHint extends Hint(Raml, Json)

object OasYamlHint extends Hint(Oas, Yaml)

object OasJsonHint extends Hint(Oas, Json)

object AmfJsonHint extends Hint(Amf, Json)

sealed trait Kind

object Library extends Kind

object Extension extends Kind

object Link extends Kind

object Unspecified extends Kind
