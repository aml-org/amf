package amf.shapes.client.platform.model.domain.jsonldinstance

import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray => InternalJsonLDArray}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDArray(override private[amf] val _internal: InternalJsonLDArray) extends JsonLDElement {
  @JSExportTopLevel("JsonLDArray")
  def this() = this(InternalJsonLDArray(Nil))

  def +=(value: JsonLDElement): Unit = _internal.+=(value)
  def jsonLDElements: ClientList[JsonLDElement] = _internal.jsonLDElements.asClient

}
