package amf.shapes.client.platform.model.document
import amf.core.client.platform.model.document.BaseUnit
import amf.shapes.client.platform.model.domain.jsonldinstance.JsonLDElement
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument => InternalJsonLDInstanceDocument}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDInstanceDocument(override private[amf] val _internal: InternalJsonLDInstanceDocument) extends BaseUnit {

  @JSExportTopLevel("JsonLDInstanceDocument")
  def this() = this(InternalJsonLDInstanceDocument())

  def withEncodes(encodes: ClientList[JsonLDElement]): this.type = {
    _internal.withEncodes(encodes.asInternal)
    this
  }

  def encodes: ClientList[JsonLDElement] = _internal.encodes.asClient
}
