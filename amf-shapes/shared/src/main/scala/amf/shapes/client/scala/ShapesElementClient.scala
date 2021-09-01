package amf.shapes.client.scala

import amf.aml.client.scala.AMLElementClient
import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.client.scala.render.AMFElementRenderer
import amf.shapes.client.scala.model.domain.{AnyShape, Example}
import amf.shapes.client.scala.render.{JsonSchemaShapeRenderer, RamlShapeRenderer}
import amf.shapes.internal.spec.common.emitter.ExampleValueRenderer
import org.yaml.model.YNode

class ShapesElementClient private[amf] (override protected val configuration: ShapesConfiguration)
    extends AMLElementClient(configuration) {

  override def getConfiguration: ShapesConfiguration = configuration

  def toJsonSchema(element: AnyShape): String    = JsonSchemaShapeRenderer.toJsonSchema(element, configuration)
  def buildJsonSchema(element: AnyShape): String = JsonSchemaShapeRenderer.buildJsonSchema(element, configuration)

  def toRamlDatatype(element: AnyShape): String = RamlShapeRenderer.toRamlDatatype(element, configuration)

  def renderExample(example: Example, mediaType: String): String =
    ExampleValueRenderer.renderExample(example, mediaType)
}
