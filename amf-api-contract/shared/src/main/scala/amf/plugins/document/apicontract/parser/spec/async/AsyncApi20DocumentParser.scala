package amf.plugins.document.apicontract.parser.spec.async
import amf.core.internal.parser.Root
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext

case class AsyncApi20DocumentParser(root: Root)(override implicit val ctx: AsyncWebApiContext)
    extends AsyncApiDocumentParser(root)(ctx)
