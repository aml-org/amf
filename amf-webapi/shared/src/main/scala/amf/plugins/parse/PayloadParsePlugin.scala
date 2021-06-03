package amf.plugins.parse

import amf.client.remod.amfcore.plugins.{LowPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.parse.AMFParsePlugin
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.exception.UnsupportedParsedDocumentException
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.parser.{ParserContext, ReferenceHandler, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.remote.{Payload, Vendor}
import amf.plugins.document.webapi.contexts.parser.raml.PayloadContext
import amf.plugins.document.webapi.parser.PayloadParser
import org.yaml.model.{YMap, YScalar}

object PayloadParsePlugin extends AMFParsePlugin {

  override val id: String = Vendor.PAYLOAD.name

  override def applies(element: Root): Boolean =
    notRAML(element) && notOAS(element) && notAsync(element) // any document can be parsed as a Payload

  override def priority: PluginPriority = LowPriority

  override def parse(document: Root, ctx: ParserContext): PayloadFragment = document.parsed match {
    case parsed: SyamlParsedDocument =>
      implicit val newCtx: PayloadContext =
        new PayloadContext(document.location, ctx.refs, ctx, options = ctx.parsingOptions)
      PayloadParser(parsed.document, document.location, document.mediatype).parseUnit()
    case _ => throw UnsupportedParsedDocumentException
  }

  override def mediaTypes: Seq[String] = Seq(
    Payload.mediaType,
    "application/payload+json",
    "application/payload+yaml"
  )

  override def validMediaTypesToReference: Seq[String] = Nil

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = false

  private def notRAML(root: Root) = root.parsed match {
    case parsed: SyamlParsedDocument => parsed.comment.isEmpty || !parsed.comment.exists(_.startsWith("%"))
    case _                           => false
  }

  private def notAsync(root: Root) = containsHeader(root, List("asyncapi"))
  private def notOAS(root: Root)   = containsHeader(root, List("swagger", "openapi"))

  private def containsHeader(root: Root, validHeaders: Seq[String]) = root.parsed match {
    case parsed: SyamlParsedDocument =>
      parsed.document.node.value match {
        case map: YMap =>
          !map.entries.exists { entry =>
            val text = entry.key.value.asInstanceOf[YScalar].text
            validHeaders.exists(text.startsWith)
          }
        case _ => true
      }
    case _ =>
      false
  }
}
