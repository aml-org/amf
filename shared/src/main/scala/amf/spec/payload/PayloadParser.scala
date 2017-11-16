package amf.spec.payload

import amf.document.Document
import amf.spec.ParserContext
import amf.spec.common.{AbstractVariables, DataNodeParser}
import org.yaml.model.{YDocument, YNode}

class PayloadParser(document: YDocument, location: String)(
  implicit ctx: ParserContext) {

  def parseUnit(): Document = {
    val parsedDocument = Document().adopted(location)
    val payload = parseNode(location, document.node)
    parsedDocument.withEncodes(payload)
    parsedDocument
  }

  def parseNode(parent: String, node: YNode) =
    DataNodeParser(node, AbstractVariables(), Some(parent)).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String)(implicit ctx: ParserContext) =
    new PayloadParser(document, location)
}
