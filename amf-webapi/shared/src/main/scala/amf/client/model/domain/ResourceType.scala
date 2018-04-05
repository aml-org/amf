package amf.client.model.domain

import amf.ProfileNames
import amf.client.convert.WebApiClientConverters._
import amf.client.model.document.BaseUnit
import amf.plugins.domain.webapi.models.templates.{ResourceType => InternalResourceType}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ResourceType(override private[amf] val _internal: InternalResourceType)
    extends AbstractDeclaration(_internal) {

  @JSExportTopLevel("model.domain.ResourceType")
  def this() = this(InternalResourceType())

  override def linkCopy(): ResourceType = _internal.linkCopy()

  def asEndpoint[T <: BaseUnit](unit: T, profile: String = ProfileNames.RAML): EndPoint =
    _internal.asEndpoint(unit._internal, profile)
}
