package amf.model.domain

import amf.ProfileNames
import amf.model.document.BaseUnit
import amf.plugins.domain.webapi.models.templates

case class Trait(private[amf] val trt: templates.Trait) extends AbstractDeclaration(trt) {
  override def linkTarget: Option[DomainElement with Linkable] =
    trt.linkTarget.map({ case l: templates.Trait => Trait(l) })

  override def linkCopy(): DomainElement with Linkable = Trait(trt.linkCopy())

  def asOperation[T <: BaseUnit](unit: T, profile: String = ProfileNames.RAML): Operation =
    Operation(trt.asOperation(unit.element, profile))
}
