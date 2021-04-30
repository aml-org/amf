package amf.client.model.domain

import amf.client.model.StrField
import amf.client.convert.shapeconverters.ShapeClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.{IriTemplateMapping => InternalIriTemplateMapping}

@JSExportAll
case class IriTemplateMapping(override private[amf] val _internal: InternalIriTemplateMapping) extends DomainElement {

  @JSExportTopLevel("model.domain.IriTemplateMapping")
  def this() = this(InternalIriTemplateMapping())

  def templateVariable: StrField = _internal.templateVariable
  def linkExpression: StrField   = _internal.linkExpression

  def withTemplateVariable(variable: String): this.type = {
    _internal.withTemplateVariable(variable)
    this
  }

  def withLinkExpression(expression: String): this.type = {
    _internal.withLinkExpression(expression)
    this
  }
}
