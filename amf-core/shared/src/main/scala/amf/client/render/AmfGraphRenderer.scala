package amf.client.render

import amf.client.convert.CoreClientConverters.ClientFuture
import amf.client.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin
import amf.core.emitter.{RenderOptions => InternalRenderOptions}
import org.yaml.builder.DocBuilder
import amf.client.convert.CoreClientConverters._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  * Amf generator.
  */
@JSExportTopLevel("AmfGraphRenderer")
class AmfGraphRenderer extends Renderer("AMF Graph", "application/ld+json") {
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)

  /** Asynchronously renders the syntax to a provided writer and returns it. */
  @JSExport
  def generateToBuilder[T](unit: BaseUnit, options: RenderOptions, builder: DocBuilder[T]): ClientFuture[Unit] =
    generate(unit._internal, InternalRenderOptions(options), builder).asClient

  /** Asynchronously renders the syntax to a provided writer and returns it. */
  @JSExport
  def generateToBuilder[T](unit: BaseUnit, builder: DocBuilder[T]): ClientFuture[Unit] =
    generateToBuilder(unit, RenderOptions(), builder)
}
