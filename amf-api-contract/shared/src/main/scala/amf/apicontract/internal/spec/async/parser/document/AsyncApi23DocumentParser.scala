package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.{AsyncApi21, AsyncApi22, AsyncApi23}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.{
  Async22DeclarationParser,
  Async23DeclarationParser
}
import amf.core.internal.parser.Root

object AsyncApi23DocumentParser {
  // Doesn't add new functionality to previous version
  def apply(root: Root)(implicit ctx: AsyncWebApiContext) =
    AsyncApi21DocumentParser(root, AsyncApi23, Async23DeclarationParser)
}
