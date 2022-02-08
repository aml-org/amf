package amf.apiinstance.internal.plugins

import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.spec.document.FlexGatewayDocumentParser
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.parser.domain.Declarations
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import org.yaml.model.{YMap, YScalar}

object FlexGatewayConfigParserPlugin extends AMFParsePlugin {
  override def spec: Spec = FLEXGW

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    FlexGatewayDocumentParser(document)(context(document,ctx)).parseDocument()
  }

  private def context(document: Root, ctx: ParserContext): FlexGWConfigContext = {
    FlexGWConfigContext(
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
  override def mediaTypes: Seq[String] = Seq(`application/yaml`, `application/json`)

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override def allowRecursiveReferences: Boolean = false

  override def applies(root: Root): Boolean = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(m) =>
            val isMuleSoft = m.key("apiVersion")match {
              case Some(entry) =>
                entry.value.as[YScalar].text == "gateway.mulesoft.com/v1alpha1"
              case _           => false
            }
            val isApiInstance = m.key("kind")match {
              case Some(entry) =>
                entry.value.as[YScalar].text == "ApiInstance"
              case _           => false
            }
            isMuleSoft && isApiInstance
          case _        => false
        }
      case _                           => false

    }
  }

  override def priority: PluginPriority = NormalPriority

}
