package amf.plugins.domain.webapi.models

import amf.core.utils._
import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel._

case class IriTemplateMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  def templateVariable: StrField = fields.field(TemplateVariable)
  def linkExpression: StrField   = fields.field(LinkExpression)

  def withTemplateVariable(variable: String): this.type = set(TemplateVariable, variable)
  def withLinkExpression(expression: String): this.type = set(LinkExpression, expression)

  override def meta: Obj = IriTemplateMappingModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = s"/mapping/${templateVariable.option().getOrElse("unknownVar").urlEncoded}"
}

object IriTemplateMapping {
  def apply(): IriTemplateMapping     = apply(Annotations())
  def apply(annotations: Annotations) = new IriTemplateMapping(Fields(), annotations)
}
