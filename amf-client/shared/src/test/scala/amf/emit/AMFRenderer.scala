package amf.emit

import amf.Core
import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.Syntax.{Json, Syntax}
import amf.core.remote._
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.{Oas20Plugin, PayloadPlugin, Raml08Plugin, Raml10Plugin, _}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// TODO: this is only here for compatibility with the test suite
class AMFRenderer(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax]) {

  Core.init()
  amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)

  /** Print ast to string. */
  def renderToString: Future[String] = render()

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String): Future[Unit] = render().flatMap(s => remote.write(path, s))

  private def render(): Future[String] = {
    val mediaType = syntax.fold(vendor match {
      case Amf                          => "application/ld+json"
      case Payload                      => "application/amf+json"
      case Raml10 | Raml08 | Raml | Aml => "application/yaml"
      case Oas | Oas20 | Oas30          => "application/json"
      case AsyncApi20 | AsyncApi        => "application/json"
      case _                            => "text/plain"
    })({
      case Json => "application/json"
      case _    => "application/yaml"
    })

    new AMFSerializer(unit, mediaType, vendor.name, options)
      .renderToString(scala.concurrent.ExecutionContext.Implicits.global)
  }
}

object AMFRenderer {
  def apply(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax] = None): AMFRenderer =
    new AMFRenderer(unit, vendor, options, syntax)
}
