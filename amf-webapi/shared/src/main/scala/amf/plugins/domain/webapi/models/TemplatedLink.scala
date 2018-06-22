package amf.plugins.domain.webapi.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel._
import amf.core.utils.Strings

case class TemplatedLink(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def name: StrField                   = fields.field(Name)
  def description: StrField            = fields.field(Description)
  def template: StrField               = fields.field(Template)
  def operationId: StrField            = fields.field(OperationId)
  def mapping: Seq[IriTemplateMapping] = fields.field(Mapping)
  def requestBody: StrField            = fields.field(RequestBody)
  def server: Server                   = fields.field(TemplatedLinkModel.Server)

  def withName(name: String): this.type                        = set(Name, name)
  def withDescription(description: String): this.type          = set(Description, description)
  def withTemplate(template: String): this.type                = set(Template, template)
  def withOperationId(operationId: String): this.type          = set(OperationId, operationId)
  def withMapping(mapping: Seq[IriTemplateMapping]): this.type = setArray(Mapping, mapping)
  def withRequestBody(requestBody: String): this.type          = set(RequestBody, requestBody)
  def withServer(server: Server): this.type                    = set(TemplatedLinkModel.Server, server)

  override def meta: Obj            = TemplatedLinkModel
  override def linkCopy(): Linkable = TemplatedLink().withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    s"/templatedLink/${name.option().getOrElse("UnknownTemplatedLink").urlComponentEncoded}"
}

object TemplatedLink {
  def apply(): TemplatedLink          = apply(Annotations())
  def apply(annotations: Annotations) = new TemplatedLink(Fields(), annotations)
}
