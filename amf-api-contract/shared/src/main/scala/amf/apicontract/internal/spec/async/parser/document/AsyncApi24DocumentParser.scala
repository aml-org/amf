package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.Async24DeclarationParser
import amf.core.internal.parser.Root
import amf.core.internal.remote.AsyncApi24

object AsyncApi24DocumentParser {
  // Doesn't add new functionality to previous version
  def apply(root: Root)(implicit ctx: AsyncWebApiContext) =
    AsyncApi21DocumentParser(root, AsyncApi24, Async24DeclarationParser)
}
