package amf.plugins.document.webapi.parser

import amf.framework.model.document.Document
import amf.framework.parser.ParserContext
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import org.yaml.model.{YDocument, YNode}

class PayloadParser(document: YDocument, location: String)(implicit ctx: ParserContext) {

  def parseUnit(): Document = {
    val parsedDocument = Document().adopted(location)
    val payload        = parseNode(location, document.node)
    parsedDocument.withEncodes(payload)
    parsedDocument
  }

  private def parseNode(parent: String, node: YNode) =
    DataNodeParser(node, parent = Some(parent)).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String)(implicit ctx: ParserContext) =
    new PayloadParser(document, location)
}
