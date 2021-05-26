package amf.convert

import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.AMFSerializer
import amf.core.model.document.BaseUnit
import org.yaml.builder.JsOutputBuilder
import org.yaml.model.YDocument
import org.yaml.parser.JsonParser
import org.yaml.render.JsonRender

import scala.concurrent.Future
import scala.scalajs.js

class JsOutputBuilderTest extends DocBuilderTest {

  override def render(unit: BaseUnit, config: CycleConfig, options: RenderOptions): Future[String] = {
    val builder: JsOutputBuilder = new JsOutputBuilder()
    val config                   = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withRenderOptions(options)
    val renderer                 = new AMFSerializer(unit, "application/graph+ldjson", config.renderConfiguration)
    renderer
      .renderToBuilder(builder)
      .map(_ => {
        val parser: JsonParser  = JsonParser(js.JSON.stringify(builder.result))
        val document: YDocument = parser.documents()(0)
        JsonRender.render(document)
      })
  }
}
