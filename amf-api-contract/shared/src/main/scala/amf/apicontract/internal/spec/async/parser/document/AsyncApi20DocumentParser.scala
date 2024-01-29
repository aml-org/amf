package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.AsyncDeclarationParser
import amf.core.internal.parser.Root

case class AsyncApi20DocumentParser(root: Root, declarationParser: AsyncDeclarationParser)(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncApiDocumentParser(root, declarationParser)(ctx)
