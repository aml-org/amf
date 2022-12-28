package amf.shapes.client.platform.model.domain.jsonldinstance

import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDError => InternalJsonLDError}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class JsonLDError(override private[amf] val _internal: InternalJsonLDError) extends JsonLDElement {
  @JSExportTopLevel("JsonLDError")
  def this() = this(InternalJsonLDError())

}
