package amf.model.domain

import amf.plugins.domain.webapi.models.templates

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class ParametrizedResourceType private[model] (
    private val resourceType: templates.ParametrizedResourceType)
    extends ParametrizedDeclaration(resourceType) {
  def this() = this(templates.ParametrizedResourceType())

  override private[amf] def element: templates.ParametrizedResourceType = resourceType
}
