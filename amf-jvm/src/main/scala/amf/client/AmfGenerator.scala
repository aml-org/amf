package amf.client

import java.io.File
import java.util.concurrent.CompletableFuture

import amf.model.BaseUnit
import amf.remote.Amf
import amf.remote.FutureConverter.converters
import amf.remote.Syntax.Json

/**
  * [[Amf]] generator.
  */
class AmfGenerator extends BaseGenerator(Amf, Json) {

  def generateFile(unit: BaseUnit, path: File, options: GenerationOptions, handler: FileHandler): Unit =
    generateSync(unit.unit, path.getAbsolutePath, options, UnitHandlerAdapter(handler))

  def generateString(unit: BaseUnit, options: GenerationOptions, handler: StringHandler): Unit =
    generateSync(unit.unit, options, StringHandlerAdapter(handler))

  def generateFileAsync(unit: BaseUnit, url: String, options: GenerationOptions): CompletableFuture[String] =
    generateAsync(unit.unit, url, options).asJava

  def generateStringAsync(unit: BaseUnit, options: GenerationOptions): CompletableFuture[String] =
    generateAsync(unit.unit, options).asJava
}
