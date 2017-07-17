package amf.remote

import amf.common.AMFToken
import amf.remote.Syntax.{Json, Syntax, Yaml}

case class Hint(vendor: Vendor, syntax: Syntax, kind: Kind = Unspecified) {
  def +(k: Kind): Hint = copy(kind = k)
}

object RamlYamlHint  extends Hint(Raml, Yaml)
object RamlJsonHint  extends Hint(Raml, Json)
object OasYamlHint   extends Hint(Oas, Yaml)
object OasJsonHint   extends Hint(Oas, Json)
object AmfJsonLdHint extends Hint(Amf, Json)

sealed trait Vendor
object Raml extends Vendor
object Oas  extends Vendor
object Amf  extends Vendor

sealed trait Kind
object Library     extends Kind
object Link        extends Kind
object Unspecified extends Kind

object Kind {
  def apply(token: AMFToken): Kind = token match {
    case AMFToken.Link    => Link
    case AMFToken.Library => Library
  }
}
