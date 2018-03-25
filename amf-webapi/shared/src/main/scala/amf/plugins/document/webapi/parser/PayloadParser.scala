package amf.plugins.document.webapi.parser

import amf.core.model.document.PayloadFragment
import amf.core.parser.ParserContext
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import org.yaml.model.{YDocument, YNode}

class PayloadParser(document: YDocument, location: String, mediaType: String)(implicit ctx: ParserContext) {

  def parseUnit(): PayloadFragment = {
    val payload        = parseNode(location, document.node)
    val parsedDocument = PayloadFragment(payload, mediaType).adopted(location)
    parsedDocument
  }

  private def parseNode(parent: String, node: YNode) =
    DataNodeParser(node, parent = Some(parent)).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String, mediaType: String)(implicit ctx: ParserContext) =
    new PayloadParser(document, location, mediaType)
}
