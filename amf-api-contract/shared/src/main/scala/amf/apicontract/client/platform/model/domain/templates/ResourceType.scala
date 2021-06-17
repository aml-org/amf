package amf.apicontract.client.platform.model.domain.templates

import amf.apicontract.client.scala.model.domain.templates.{ResourceType => InternalResourceType}
import amf.apicontract.internal.convert.ApiClientConverters.{ClientOption, _}
import amf.core.client.platform.model.domain.{AbstractDeclaration, DomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ResourceType(override private[amf] val _internal: InternalResourceType)
    extends AbstractDeclaration(_internal) {

  @JSExportTopLevel("ResourceType")
  def this() = this(InternalResourceType())

  override def linkTarget: ClientOption[DomainElement] = _internal.linkTarget.asClient

  override def linkCopy(): ResourceType = _internal.linkCopy()

}
