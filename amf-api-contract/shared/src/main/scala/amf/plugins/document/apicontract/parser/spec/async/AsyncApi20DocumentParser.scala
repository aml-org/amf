package amf.plugins.document.apicontract.parser.spec.async
import amf.core.internal.parser.Root
import amf.shapes.internal.spec.contexts.parser.async.AsyncWebApiContext

case class AsyncApi20DocumentParser(root: Root)(override implicit val ctx: AsyncWebApiContext)
    extends AsyncApiDocumentParser(root)(ctx)
