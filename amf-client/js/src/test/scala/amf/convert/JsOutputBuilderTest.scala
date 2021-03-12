package amf.convert

import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
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
    val renderer                 = AMFSerializer(unit, "application/ld+json", "AMF Graph", options)
    renderer
      .renderToBuilder(builder)
      .map(_ => {
        val parser: JsonParser  = JsonParser(js.JSON.stringify(builder.result))
        val document: YDocument = parser.documents()(0)
        JsonRender.render(document)
      })
  }
}
