package amf.shapes.internal.plugins.render

import amf.shapes.client.scala.config.JsonLDSchemaConfiguration
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.yaml.model.YNode
import org.yaml.render.{JsonRender, YamlRender}

object JsonLDInstanceRenderHelper {

  private lazy val config = JsonLDSchemaConfiguration.JsonLDSchema()

  private def renderAsYNode(element: JsonLDObject): YNode = config.elementClient().renderElement(element)

  def renderToJson(element: JsonLDObject): String = JsonRender.render(renderAsYNode(element))

  def renderToYaml(element: JsonLDObject): String = YamlRender.render(renderAsYNode(element))
}
