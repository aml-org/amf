package amf.apicontract.internal.spec.async.parser.domain.declarations

object Async21DeclarationParser {

  // Doesn't add new functionality to previous version
  def apply(): AsyncDeclarationParser = Async20DeclarationParser()
}
