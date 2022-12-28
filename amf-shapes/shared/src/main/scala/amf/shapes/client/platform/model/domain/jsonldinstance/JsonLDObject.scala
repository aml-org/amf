package amf.shapes.client.platform.model.domain.jsonldinstance

import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDObject => InternalJsonLDObject}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDObject(override private[amf] val _internal: InternalJsonLDObject) extends DomainElement with JsonLDElement {
  @JSExportTopLevel("JsonLDObject")
  def this() = this(InternalJsonLDObject.empty(new JsonLDEntityModel(Nil, Nil, JsonPath.empty), JsonPath.empty))
  def componentId: String = _internal.componentId

}
