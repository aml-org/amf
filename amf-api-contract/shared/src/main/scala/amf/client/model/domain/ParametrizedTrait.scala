package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.ParametrizedDeclaration
import amf.plugins.domain.apicontract.models.templates.{ParametrizedTrait => InternalParametrizedTrait}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedTrait(override private[amf] val _internal: InternalParametrizedTrait)
    extends ParametrizedDeclaration {

  @JSExportTopLevel("model.domain.ParametrizedTrait")
  def this() = this(InternalParametrizedTrait())
}
