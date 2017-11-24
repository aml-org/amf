package amf.client

import amf.core.client.GenerationOptions
import amf.framework.remote.Amf
import amf.model.BaseUnit
import amf.framework.remote.Syntax.Json

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  * [[Amf]] generator.
  */
@JSExportTopLevel("AmfGenerator")
class AmfGenerator extends BaseGenerator(Amf, Json) {

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions, handler: FileHandler): Unit =
    generate(unit.element, url, options, UnitHandlerAdapter(handler))

  @JSExport
  def generateString(unit: BaseUnit, options: GenerationOptions, handler: StringHandler): Unit =
    generate(unit.element, options, StringHandlerAdapter(handler))

  @JSExport
  def generateFile(unit: BaseUnit, url: String, options: GenerationOptions): js.Promise[Unit] =
    generate(unit.element, url, options).toJSPromise

  @JSExport
  def generateString(unit: BaseUnit, options: GenerationOptions): String =
    generate(unit.element, options)
}
