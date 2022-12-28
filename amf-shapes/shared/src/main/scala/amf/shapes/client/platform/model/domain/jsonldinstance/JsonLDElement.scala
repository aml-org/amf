package amf.shapes.client.platform.model.domain.jsonldinstance

import amf.core.client.platform.model.{Annotable, Annotations}
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement => InternalJsonLDElement}
import amf.shapes.internal.convert.ShapeClientConverters._

trait JsonLDElement extends Annotable {

  private[amf] val _internal: InternalJsonLDElement

  override def annotations(): Annotations = _internal.annotations

}
