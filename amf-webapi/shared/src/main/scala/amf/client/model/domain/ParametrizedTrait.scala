package amf.client.model.domain

import amf.plugins.domain.webapi.models.templates.{ParametrizedTrait => InternalParametrizedTrait}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedTrait(override private[amf] val _internal: InternalParametrizedTrait)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedTrait")
  def this() = this(InternalParametrizedTrait())
}
