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

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val builder: JsOutputBuilder = new JsOutputBuilder()
    val options                  = RenderOptions().withSourceMaps.withPrettyPrint.withAmfJsonLdSerialization
    val renderer                 = new AMFSerializer(unit, "application/ld+json", "AMF Graph", options)
    renderer
      .renderToBuilder(builder)
      .map(_ => {
        val jsObject: js.Any = builder.result
        val content: String  = js.JSON.stringify(jsObject)
        val document         = JsonParser.apply(content).parse().head.asInstanceOf[YDocument]
        JsonRender.render(document)
      })
  }
}
