package amf.apicontract.internal.plugins

import amf.apicontract.internal.spec.common.reference.JsonRefsReferenceHandler
import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.plugins.parse.{DomainParsingFallback, ExternalFragmentDomainFallback}
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.{JSONRefs, Spec}
import amf.core.internal.utils.MediaTypeMatcher
import amf.core.internal.validation.CoreParserValidations.{CantReferenceSpecInFileTree, CouldntGuessRoot}

// TODO ARM: change this, refactor to have different options based on the configuration (Raml strict, WebApi Relaxed).
// TODO ARM: Export withFallback and this object to the user. User cannot create instance or implement interface? yes? no?
case class ApiContractFallbackPlugin(strict: Boolean = true, skipWarnings: Boolean = false) extends DomainParsingFallback {

  private def docMediaType(doc: Root) = if (doc.raw.isJson) `application/json` else `application/yaml`

  override def chooseFallback(root: Root, availablePlugins: Seq[AMFParsePlugin], isRoot: Boolean): AMFParsePlugin = {
    if (strict && isRoot) throw UnsupportedDomainForDocumentException(root.location)
    root.parsed match {
      case parsed: SyamlParsedDocument if !root.raw.isXml => ApiContractDomainFallbackPlugin(parsed, isRoot)
      case _ => ExternalFragmentDomainFallback(strict).chooseFallback(root, availablePlugins, isRoot)
    }
  }

  def plugin(parsed: SyamlParsedDocument): ApiContractDomainFallbackPlugin = ApiContractDomainFallbackPlugin(parsed)

  case class ApiContractDomainFallbackPlugin(parsed: SyamlParsedDocument, isRoot: Boolean = false) extends AMFParsePlugin {
    override def spec: Spec = JSONRefs

    override def validSpecsToReference: Seq[Spec] = Seq(JSONRefs)
    override def parse(document: Root, ctx: ParserContext): BaseUnit = {
      throwUserFriendlyWarnings(document, ctx)
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
    }

    private def throwUserFriendlyWarnings(document: Root, ctx: ParserContext) = {
      if (isRoot) ctx.eh.warning(CouldntGuessRoot, "", None, s"Couldn't guess spec for root file", None, Some(document.location))
      else if (!skipWarnings){
        pluginThatMatches(document, ctx.config.sortedParsePlugins).foreach { spec =>
          ctx.eh.warning(CantReferenceSpecInFileTree, "", None, s"Document identified as ${spec.id} is of different spec from root", None, Some(document.location))
        }
      }
    }

    private def pluginThatMatches(document: Root, plugins: Seq[AMFParsePlugin]): Option[Spec] = {
      plugins.find(_.applies(document)).map(_.spec)
    }

    override val priority: PluginPriority = LowPriority

    override def mediaTypes: Seq[String] = Seq(
      `application/json`,
      `application/yaml`,
    )

    override def applies(document: Root): Boolean = !document.raw.isXml // for JSON or YAML

    override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new JsonRefsReferenceHandler()

    override def allowRecursiveReferences: Boolean = true
  }
}
