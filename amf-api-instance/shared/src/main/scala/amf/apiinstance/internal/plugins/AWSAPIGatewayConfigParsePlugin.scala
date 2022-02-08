package amf.apiinstance.internal.plugins

import amf.apiinstance.internal.spec.context.AWSAPIGWConfigContext
import amf.apiinstance.internal.spec.document.{AWSAPIGatewayConfigDocumentParser, KongDeclarativeConfigDocumentParser}
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Declarations
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec


object AWSAPIGatewayConfigParsePlugin extends AMFParsePlugin with NodeTraverser {
  override def spec: Spec = Spec.AWSAPIGWCONFIG

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    AWSAPIGatewayConfigDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  private def context(document: Root, ctx: ParserContext): AWSAPIGWConfigContext = {
    AWSAPIGWConfigContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(Declarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations))
    )
  }

  override def mediaTypes: Seq[String] = Seq(`application/yaml`)

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = false

  override def applies(root: Root): Boolean = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        val root = traverse(parsed.document.node)
        if (root.fetch("AWSTemplateFormatVersion").string().isDefined) {
          root.fetch("Resources").mapOr(false) { resources =>
            resources.map.values.exists { resource =>
              traverse(resource).fetch("Type").string().contains("AWS::ApiGatewayV2::Api")
            }
          }
        } else {
          false
        }

      case _              => false
    }
  }

  override def priority: PluginPriority = NormalPriority

  override def error_handler: AMFErrorHandler = DefaultErrorHandler()
}
