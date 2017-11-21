package amf.plugins.domain.payload

import amf.core.Root
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.payload.parser.PayloadParser
import amf.spec.ParserContext

class PayloadPlugin extends AMFDomainPlugin {

  override val ID = "AMF Payload"

  // we are looking for documents with a very specific payload
  // otherwise, this plugin can become the fallback option.
  // Fallback option should be an external fragment.
  override def domainSyntaxes = Seq(
    "application/amf+json",
    "application/amf+yaml"
  )

  override def parse(root: Root, parentContext: ParserContext) = {
    implicit val ctx = parentContext
    Some(PayloadParser(root.parsed.document, root.location).parseUnit())
  }

  override def accept(root: Root) = true // any document can be parsed as a Payload
  override def referenceCollector() = new PayloadReferenceCollector
}
