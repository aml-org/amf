package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.AsyncApi25
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.Async25DeclarationParser
import amf.core.internal.parser.Root

object AsyncApi25DocumentParser {
  // Doesn't add new functionality to previous version
  def apply(root: Root)(implicit ctx: AsyncWebApiContext) =
    AsyncApi21DocumentParser(root, AsyncApi25, Async25DeclarationParser())
}
