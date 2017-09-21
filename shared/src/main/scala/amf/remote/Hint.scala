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

sealed trait Vendor {
  val name: String
  val defaultSyntax: Syntax
}

object Raml extends Vendor {
  override val name: String          = "raml"
  override val defaultSyntax: Syntax = Yaml
}

object Oas extends Vendor {
  override val name: String          = "oas"
  override val defaultSyntax: Syntax = Json
}

object Amf extends Vendor {
  override val name: String          = "amf"
  override val defaultSyntax: Syntax = Json
}

sealed trait Kind

object Library extends Kind

object Link extends Kind

object Unspecified extends Kind

object Vendor {
  def unapply(name: String): Option[Vendor] = {
    name match {
      case "raml" => Some(Raml)
      case "oas"  => Some(Oas)
      case "amf"  => Some(Amf)
      case _      => None
    }
  }
}
