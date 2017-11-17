package amf.client

import java.io.File
import java.util.concurrent.CompletableFuture

import amf.model.BaseUnit
import amf.remote.Amf
import amf.remote.FutureConverter.converters
import amf.remote.Syntax.Json

/**
  * [[amf.remote.Amf]] generator.
  */
class AmfGenerator extends BaseGenerator(Amf, Json) {

  def generateFile(unit: BaseUnit, path: File, options: GenerationOptions, handler: FileHandler): Unit =
    generate(unit.element, path.getAbsolutePath, options, UnitHandlerAdapter(handler))

  def generateString(unit: BaseUnit, options: GenerationOptions, handler: StringHandler): Unit =
    generate(unit.element, options, StringHandlerAdapter(handler))

  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions): CompletableFuture[String] =
    generate(unit.element, url, options).asJava

  def generateString(unit: BaseUnit, options: GenerationOptions): String =
    generate(unit.element, options)
}
