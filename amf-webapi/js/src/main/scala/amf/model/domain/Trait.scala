package amf.model.domain

import amf.ProfileNames
import amf.model.document.BaseUnit
import amf.plugins.domain.webapi.models.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Trait(private[amf] val trt: templates.Trait) extends AbstractDeclaration(trt) {

  @JSExportTopLevel("model.domain.Trait")
  def this() = this(templates.Trait())

  override private[amf] def element = trt

  override def linkTarget: Option[DomainElement with Linkable] =
    trt.linkTarget.map({ case l: templates.Trait => Trait(l) })

  override def linkCopy(): DomainElement with Linkable = Trait(trt.linkCopy())

  def asOperation[T <: BaseUnit](unit: T, profile: String = ProfileNames.RAML): Operation =
    Operation(trt.asOperation(unit.element, profile))
}
