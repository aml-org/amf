package amf.model.domain

import amf.plugins.domain.webapi.models.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedResourceType private[model] (private val resourceType: templates.ParametrizedResourceType)
    extends ParametrizedDeclaration(resourceType) {

  @JSExportTopLevel("model.domain.ParametrizedResourceType")
  def this() = this(templates.ParametrizedResourceType())

  override private[amf] def element: templates.ParametrizedResourceType = resourceType
}
