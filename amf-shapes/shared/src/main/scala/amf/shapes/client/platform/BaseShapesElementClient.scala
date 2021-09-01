package amf.shapes.client.platform

import amf.aml.client.platform.BaseAMLElementClient
import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.render.AMFElementRenderer
import amf.shapes.client.platform.model.domain.{AnyShape, Example}
import amf.shapes.client.scala.{ShapesElementClient => InternalShapesElementClient}
import amf.shapes.internal.convert.ShapeClientConverters._
import org.yaml.builder.DocBuilder

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class BaseShapesElementClient private[amf] (private val _internal: InternalShapesElementClient)
    extends BaseAMLElementClient(_internal) {

  private[amf] def this(configuration: ShapesConfiguration) = {
    this(new InternalShapesElementClient(configuration))
  }

  def toJsonSchema(element: AnyShape): String                    = _internal.toJsonSchema(element)
  def buildJsonSchema(element: AnyShape): String                 = _internal.buildJsonSchema(element)
  def toRamlDatatype(element: AnyShape): String                  = _internal.toRamlDatatype(element)
  def renderExample(example: Example, mediaType: String): String = _internal.renderExample(example, mediaType)

  override def renderToBuilder[T](element: DomainElement, builder: DocBuilder[T]): Unit =
    AMFElementRenderer.renderToBuilder(element, builder, getConfiguration())
}
