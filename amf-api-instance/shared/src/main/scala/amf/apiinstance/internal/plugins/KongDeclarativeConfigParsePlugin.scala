package amf.apiinstance.internal.plugins

import amf.apiinstance.internal.spec.context.KongDeclarativeConfigContext
import amf.apiinstance.internal.spec.document.KongDeclarativeConfigDocumentParser
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

object KongDeclarativeConfigParsePlugin extends AMFParsePlugin {
  override def spec: Spec = Spec.KONG

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    KongDeclarativeConfigDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  private def context(document: Root, ctx: ParserContext): KongDeclarativeConfigContext = {
    new KongDeclarativeConfigContext(
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
        parsed.document.to[YMap] match {
          case Right(map) =>
            map.key("_format_version").isDefined
          case Left(_) => false
        }
      case _              => false
    }
  }

  override def priority: PluginPriority = NormalPriority
}
