package amf.remote

case class Hint(syntax: String, vendor: String)

object RamlYamlHint extends Hint(Vendor.raml, Syntax.yaml)
object RamlJsonHint extends Hint(Vendor.raml, Syntax.json)
object OasYamlHint  extends Hint(Vendor.oas, Syntax.yaml)
object OasJsonHint  extends Hint(Vendor.oas, Syntax.json)

object Syntax {
  val yaml = "yaml"
  val json = "json"
}

object Vendor {
  val oas  = "oas"
  val raml = " raml"
}
