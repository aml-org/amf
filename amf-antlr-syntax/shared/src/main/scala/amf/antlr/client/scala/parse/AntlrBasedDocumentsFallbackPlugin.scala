package amf.antlr.client.scala.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.antlr.client.scala.parse.syntax.SourceASTElement
import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{
  CompilerReferenceCollector,
  ParsedDocument,
  ParserContext,
  ReferenceHandler,
  SyamlParsedDocument
}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.plugins.parse.{DomainParsingFallback, ExternalFragmentDomainFallback}
import amf.core.internal.remote.{Spec, Syntax}
import amf.core.internal.validation.CoreParserValidations.{CantReferenceSpecInFileTree, CouldntGuessRoot}

case class AntlrBasedDocumentsFallbackPlugin(
    strict: Boolean,
    skipValidations: Boolean = false,
    validationSeverity: String,
    spec: Spec
) extends DomainParsingFallback {
  override def chooseFallback(root: Root, availablePlugins: Seq[AMFParsePlugin], isRoot: Boolean): AMFParsePlugin = {
    if (strict && isRoot) throw UnsupportedDomainForDocumentException(root.location)
    root.parsed match {
      case parsed: AntlrParsedDocument =>
        AntlrBasedDomainFallbackPlugin(parsed, validationSeverity, isRoot, spec)
      case _ => ExternalFragmentDomainFallback(strict).chooseFallback(root, availablePlugins, isRoot)
    }
  }

  case class AntlrBasedDomainFallbackPlugin(
      parsed: AntlrParsedDocument,
      validationSeverity: String,
      isRoot: Boolean = false,
      configSpec: Spec
  ) extends AMFParsePlugin {
    override def spec: Spec = configSpec

    override def validSpecsToReference: Seq[Spec] = Seq(configSpec)

    override def parse(document: Root, ctx: ParserContext): BaseUnit = {
      throwUserFriendlyValidations(document, ctx)
      val annotations = parsed.ast.rootOption().map(r => Annotations(SourceASTElement(r))).getOrElse(Annotations())
      val result =
        ExternalDomainElement(annotations)
          .withId(document.location + "#/")
          .withRaw(document.raw)
          .withMediaType(document.mediatype)
      val references = document.references.map(_.unit)
      val fragment = ExternalFragment()
        .withLocation(document.location)
        .withId(document.location)
        .withEncodes(result)
        .withLocation(document.location)
      if (references.nonEmpty) fragment.withReferences(references)
      fragment
    }

    private def throwUserFriendlyValidations(document: Root, ctx: ParserContext): Unit = {
      if (isRoot) {
        validationSeverity match {
          case SeverityLevels.WARNING =>
            ctx.eh
              .warning(CouldntGuessRoot, "", None, s"Couldn't guess spec for root file", None, Some(document.location))
          case SeverityLevels.VIOLATION =>
            ctx.eh.violation(
              CouldntGuessRoot,
              "",
              None,
              s"Couldn't guess spec for root file",
              None,
              Some(document.location)
            )
        }
      } else if (!skipValidations) {
        pluginThatMatches(document, ctx.config.sortedReferenceParsePlugins).foreach { spec =>
          ctx.eh.warning(
            CantReferenceSpecInFileTree,
            "",
            None,
            s"Document identified as ${spec.id} is of different spec from root",
            None,
            Some(document.location)
          )
        }
      }
    }

    private def pluginThatMatches(document: Root, plugins: Seq[AMFParsePlugin]): Option[Spec] = {
      plugins.find(_.applies(document)).map(_.spec)
    }

    override val priority: PluginPriority = LowPriority

    override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq ++ Syntax.proto3Mimes.toSeq

    override def applies(document: Root): Boolean = true // for JSON or YAML

    override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = (_: ParsedDocument, _: ParserContext) =>
      CompilerReferenceCollector()

    override def allowRecursiveReferences: Boolean = true
  }
}
