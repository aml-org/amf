package amf.remote

import amf.remote.Syntax.{json, yaml}
import amf.remote.Vendor.{oas, raml}

case class Hint(syntax: String, vendor: String)

object RamlYamlHint extends Hint(raml, yaml)
object RamlJsonHint extends Hint(raml, json)
object OasYamlHint  extends Hint(oas, yaml)
object OasJsonHint  extends Hint(oas, json)

object Syntax {
  val yaml = "yaml"
  val json = "json"
}

object Vendor {
  val oas  = "oas"
  val raml = " raml"
}
