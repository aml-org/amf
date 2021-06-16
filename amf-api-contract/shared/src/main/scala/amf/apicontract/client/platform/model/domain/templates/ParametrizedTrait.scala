package amf.apicontract.client.platform.model.domain.templates

import amf.core.client.platform.model.domain.ParametrizedDeclaration
import amf.apicontract.client.scala.model.domain.templates.{ParametrizedTrait => InternalParametrizedTrait}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedTrait(override private[amf] val _internal: InternalParametrizedTrait)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedTrait")
  def this() = this(InternalParametrizedTrait())
}
