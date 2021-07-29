package amf.apicontract.internal.spec.payload

import amf.apicontract.internal.spec.raml.parser.context.PayloadContext
import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.UnsupportedParsedDocumentException
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{
  ParserContext,
  ReferenceHandler,
  SimpleReferenceHandler,
  SyamlParsedDocument
}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import org.yaml.model.{YMap, YScalar}

object PayloadParsePlugin extends AMFParsePlugin {

  override val id: String = Spec.PAYLOAD.id

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

  override def mediaTypes: Seq[String] = PayloadMediaTypes.mediaTypes

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
