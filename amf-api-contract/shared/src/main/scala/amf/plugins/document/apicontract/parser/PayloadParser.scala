package amf.plugins.document.apicontract.parser

import amf.core.client.scala.model.document.PayloadFragment
import amf.plugins.document.apicontract.contexts.WebApiContext
import amf.plugins.document.apicontract.parser.spec.common.DataNodeParser
import org.yaml.model.{YDocument, YNode}

class PayloadParser(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) {

  def parseUnit(): PayloadFragment = {
    val payload        = parseNode(location, document.node)
    val parsedDocument = PayloadFragment(payload, mediaType).adopted(location)
    parsedDocument
  }

  private def parseNode(parent: String, node: YNode) =
    DataNodeParser(node, parent = Some(parent))(WebApiShapeParserContextAdapter(ctx)).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) =
    new PayloadParser(document, location, mediaType)
}
