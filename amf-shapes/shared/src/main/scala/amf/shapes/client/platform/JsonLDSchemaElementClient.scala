package amf.shapes.client.platform

import amf.aml.client.platform.BaseAMLElementClient
import amf.core.client.platform.model.domain.DomainElement
import amf.core.internal.render.YNodeDocBuilderPopulator
import amf.shapes.client.platform.config.JsonLDSchemaConfiguration
import amf.shapes.client.scala.{JsonLDSchemaElementClient => InternalJsonLDSchemaElementClient}
import amf.shapes.internal.convert.ShapeClientConverters._
import org.yaml.builder.DocBuilder

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JsonLDSchemaElementClient private[amf] (private[amf] val _internal: InternalJsonLDSchemaElementClient)
    extends BaseAMLElementClient(_internal) {

  private[amf] def this(configuration: JsonLDSchemaConfiguration) = {
    this(new InternalJsonLDSchemaElementClient(configuration))
  }

  override def getConfiguration(): JsonLDSchemaConfiguration = _internal.getConfiguration

  override def renderToBuilder[T](element: DomainElement, builder: DocBuilder[T]): Unit = {
    val node = _internal.renderElement(element, Nil)
    YNodeDocBuilderPopulator.populate(node, builder)
  }
}
