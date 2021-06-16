package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{IriTemplateMapping => InternalIriTemplateMapping}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

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
