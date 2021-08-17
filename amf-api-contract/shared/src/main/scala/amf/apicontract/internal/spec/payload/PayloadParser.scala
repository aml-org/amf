package amf.apicontract.internal.spec.payload

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.core.client.scala.model.document.PayloadFragment
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.datanode.DataNodeParser
import org.yaml.model.{YDocument, YNode}

class PayloadParser(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) {

  def parseUnit(): PayloadFragment = {
    val payload        = parseNode(location, document.node)
    val parsedDocument = PayloadFragment(payload, mediaType).adopted(location)
    parsedDocument
  }

  private def parseNode(parent: String, node: YNode) =
    DataNodeParser(node).parse()
}

object PayloadParser {
  def apply(document: YDocument, location: String, mediaType: String)(implicit ctx: WebApiContext) =
    new PayloadParser(document, location, mediaType)
}
