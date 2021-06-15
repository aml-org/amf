package amf.cli.internal.convert

import amf.client.environment.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.AMFSerializer
import org.yaml.builder.JsOutputBuilder
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser
import org.yaml.render.JsonRender

import scala.concurrent.Future
import scala.scalajs.js

class JsOutputBuilderTest extends DocBuilderTest {

  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    val builder: JsOutputBuilder = new JsOutputBuilder()
    val result: js.Any           = amfConfig.createClient().renderGraphToBuilder(unit, builder)
    val parser: JsonParser       = JsonParser(js.JSON.stringify(result))
    val document: YDocument      = parser.documents()(0)
    JsonRender.render(document)
  }
}
