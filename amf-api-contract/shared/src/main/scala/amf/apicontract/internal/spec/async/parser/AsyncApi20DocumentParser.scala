package amf.apicontract.internal.spec.async.parser

import amf.core.internal.parser.Root

case class AsyncApi20DocumentParser(root: Root)(override implicit val ctx: AsyncWebApiContext)
    extends AsyncApiDocumentParser(root)(ctx)
