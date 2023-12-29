package amf.shapes.client.platform.model.domain.jsonldinstance
import amf.core.client.platform.model
import amf.core.client.platform.model.Annotations
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDScalar => InternalJsonLDScalar}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDScalar(private[amf] val _internal: InternalJsonLDScalar) extends JsonLDElement {

  val value: Any       = _internal.value
  val dataType: String = _internal.dataType

  @JSExportTopLevel("JsonLDScalar")
  def this(value: Any, dataType: String, annotations: Annotations) = this(new InternalJsonLDScalar(value, dataType, annotations))

  override def annotations(): model.Annotations = _internal.annotations
}
