package amf.remote

case class Hint(vendor: Vendor, syntax: Syntax)

object RamlYamlHint extends Hint(Raml, Yaml)
object RamlJsonHint extends Hint(Raml, Json)
object OasYamlHint  extends Hint(Oas, Yaml)
object OasJsonHint  extends Hint(Oas, Json)

case class Syntax(syntax: String)
object Yaml extends Syntax("yaml")
object Json extends Syntax("json")

case class Vendor(vendor: String)
object Raml extends Vendor("raml")
object Oas  extends Vendor("oas")
