package amf.apicontract.client.platform.model.domain.templates

import amf.apicontract.client.platform.model.domain.Operation
import amf.apicontract.internal.convert.ApiClientConverters.ClientOption
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.model.domain.{AbstractDeclaration, DomainElement}
import amf.apicontract.client.scala.model.domain.templates.{Trait => InternalTrait}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Trait(override private[amf] val _internal: InternalTrait) extends AbstractDeclaration(_internal) {

  @JSExportTopLevel("model.domain.Trait")
  def this() = this(InternalTrait())

  override def linkTarget: ClientOption[DomainElement] = _internal.linkTarget.asClient

  override def linkCopy(): Trait = _internal.linkCopy()
}
