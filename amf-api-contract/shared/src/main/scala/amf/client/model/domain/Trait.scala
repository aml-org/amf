package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.document.BaseUnit
import amf.plugins.domain.apicontract.models.templates.{Trait => InternalTrait}
import amf.{ProfileName, Raml10Profile}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Trait(override private[amf] val _internal: InternalTrait) extends AbstractDeclaration(_internal) {

  @JSExportTopLevel("model.domain.Trait")
  def this() = this(InternalTrait())

  override def linkTarget: ClientOption[DomainElement] = _internal.linkTarget.asClient

  override def linkCopy(): Trait = _internal.linkCopy()

  def asOperation[T <: BaseUnit](unit: T, profile: ProfileName = Raml10Profile): Operation =
    Operation(_internal.asOperation(unit._internal, profile))
}
