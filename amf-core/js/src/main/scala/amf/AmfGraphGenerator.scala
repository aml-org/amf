package amf

import amf.core.client.{FileHandler, GenerationOptions, Generator, StringHandler}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Amf
import amf.client.model.document.BaseUnit
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * [[Amf]] generator.
  */
@JSExportAll
class AmfGraphGenerator extends Generator("AMF Graph", "application/ld+json") {

  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions, handler: FileHandler): Unit =
    generate(unit._internal, url, options, UnitHandlerAdapter(handler))

  @JSExport
  def generateString(unit: BaseUnit, options: GenerationOptions, handler: StringHandler): Unit =
    generate(unit._internal, options, StringHandlerAdapter(handler))

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions): js.Promise[Unit] =
    generate(unit._internal, url, options).toJSPromise

  @JSExport
  def generateString(unit: BaseUnit, options: GenerationOptions): String =
    generate(unit._internal, options)
}
