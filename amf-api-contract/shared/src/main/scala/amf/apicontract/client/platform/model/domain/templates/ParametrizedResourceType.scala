package amf.apicontract.client.platform.model.domain.templates

import amf.core.client.platform.model.domain.ParametrizedDeclaration
import amf.apicontract.client.scala.model.domain.templates.{ParametrizedResourceType => InternalParametrizedResourceType}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedResourceType(override private[amf] val _internal: InternalParametrizedResourceType)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedResourceType")
  def this() = this(InternalParametrizedResourceType())
}
