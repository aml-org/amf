package amf.emit

import amf.Core
import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.AMFSerializer
import amf.core.model.document.BaseUnit
import amf.core.remote.Syntax.Syntax
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.webapi.{Oas20Plugin, PayloadPlugin, Raml08Plugin, Raml10Plugin, _}
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.concurrent.{ExecutionContext, Future}

// TODO: this is only here for compatibility with the test suite
class AMFRenderer(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax])
    extends PlatformSecrets {

  Core.init()
  // Remod registering
  VocabulariesRegister.register(platform)
  amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(APIDomainPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)

  /** Print ast to string. */
  def renderToString(implicit executionContext: ExecutionContext): String = render()

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String)(implicit executionContext: ExecutionContext): Future[Unit] = {
    val result = render()
    remote.write(path, result)
  }

  private def render()(implicit executionContext: ExecutionContext): String = {
    val config = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withRenderOptions(options)
    new AMFSerializer(unit, vendor.mediaType, config.renderConfiguration).renderToString
  }
}

object AMFRenderer {
  def apply(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax] = None): AMFRenderer =
    new AMFRenderer(unit, vendor, options, syntax)
}
