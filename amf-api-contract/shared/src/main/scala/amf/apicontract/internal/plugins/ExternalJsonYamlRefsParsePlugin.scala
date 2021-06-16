package amf.apicontract.internal.plugins

import amf.apicontract.internal.spec.common.reference.JsonRefsReferenceHandler
import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.UnsupportedParsedDocumentException
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.MediaTypeMatcher

object ExternalJsonYamlRefsParsePlugin extends AMFParsePlugin {

  override val priority: PluginPriority = LowPriority

  override val id: String = "JSON + Refs"

  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(
    "application/json",
    "application/refs+json",
    "application/yaml",
    "application/refs+yaml"
  )

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    document.parsed match {
      case parsed: SyamlParsedDocument =>
        val result =
          ExternalDomainElement(Annotations(parsed.document))
            .withId(document.location + "#/")
            .withRaw(document.raw)
            .withMediaType(docMediaType(document))
        result.parsed = Some(parsed.document.node)
        val references = document.references.map(_.unit)
        val fragment = ExternalFragment()
          .withLocation(document.location)
          .withId(document.location)
          .withEncodes(result)
          .withLocation(document.location)
        if (references.nonEmpty) fragment.withReferences(references)
        fragment
      case _ => throw UnsupportedParsedDocumentException
    }
  }

  private def docMediaType(doc: Root) = if (doc.raw.isJson) "application/json" else "application/yaml"

  override def applies(document: Root): Boolean = !document.raw.isXml // for JSON or YAML

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new JsonRefsReferenceHandler()

  /**
    * media types which specifies vendors that may be referenced.
    */
  override def validMediaTypesToReference: Seq[String] = Nil

  override def allowRecursiveReferences: Boolean = true
}
