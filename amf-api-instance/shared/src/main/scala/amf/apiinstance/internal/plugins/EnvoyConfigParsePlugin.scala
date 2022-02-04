package amf.apiinstance.internal.plugins

import amf.apiinstance.internal.spec.context.AWSAPIGWConfigContext
import amf.apiinstance.internal.spec.document.EnvoyConfigDocumentParser
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.parser.domain.Declarations
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import org.yaml.model.YMap

object EnvoyConfigParsePlugin extends AMFParsePlugin {
  override def spec: Spec = Spec.ENVOY

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    EnvoyConfigDocumentParser(document)(context(document,ctx)).parseDocument()
  }

  private def context(document: Root, ctx: ParserContext): AWSAPIGWConfigContext = {
    new AWSAPIGWConfigContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(Declarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations))
    )
  }
  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Seq(`application/yaml`)

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = false

  override def applies(root: Root): Boolean = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) =>
            map.key("static_resources").isDefined
          case Left(_) => false
        }
      case _              => false
    }
  }

  override def priority: PluginPriority = NormalPriority
}
