package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.domain.ParametrizedDeclaration

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedResourceType(override private[amf] val _internal: InternalParametrizedResourceType)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedResourceType")
  def this() = this(InternalParametrizedResourceType())
}
