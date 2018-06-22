package amf.client.model.domain

import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType => InternalParametrizedResourceType}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedResourceType(override private[amf] val _internal: InternalParametrizedResourceType)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedResourceType")
  def this() = this(InternalParametrizedResourceType())
}
