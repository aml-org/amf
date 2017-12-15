package amf.model.domain

import amf.plugins.domain.webapi.models.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.ResourceType")
@JSExportAll
case class ResourceType(private[amf] val resourceType: templates.ResourceType) extends AbstractDeclaration(resourceType) {

  override protected[amf] def element = resourceType

  override def linkTarget: Option[DomainElement with Linkable] =
    resourceType.linkTarget.map({ case l: templates.ResourceType => ResourceType(l) })

  override def linkCopy(): DomainElement with Linkable = ResourceType(resourceType.linkCopy())
}
