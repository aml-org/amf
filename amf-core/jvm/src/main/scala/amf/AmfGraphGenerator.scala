package amf

import java.io.File
import java.util.concurrent.CompletableFuture

import amf.core.client.{FileHandler, GenerationOptions, Generator, StringHandler}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Amf
import amf.core.remote.FutureConverter._
import amf.client.model.document.BaseUnit
import amf.plugins.document.graph.AMFGraphPlugin

/**
  * [[Amf]] generator.
  */
class AmfGraphGenerator extends Generator("AMF Graph", "application/ld+json") {

  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)

  def generateFile(unit: BaseUnit, path: File, options: GenerationOptions, handler: FileHandler): Unit =
    generate(unit._internal, path.getAbsolutePath, options, UnitHandlerAdapter(handler))

  def generateString(unit: BaseUnit, options: GenerationOptions, handler: StringHandler): Unit =
    generate(unit._internal, options, StringHandlerAdapter(handler))

  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions): CompletableFuture[Void] =
    generate(unit._internal, url, options).asJava

  def generateString(unit: BaseUnit, options: GenerationOptions): String =
    generate(unit._internal, options)
}
