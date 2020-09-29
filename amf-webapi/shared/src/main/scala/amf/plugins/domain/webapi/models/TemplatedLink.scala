package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel._
import amf.core.utils.AmfStrings

case class TemplatedLink(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def description: StrField            = fields.field(Description)
  def template: StrField               = fields.field(Template)
  def operationId: StrField            = fields.field(OperationId)
  def operationRef: StrField           = fields.field(OperationRef)
  def mapping: Seq[IriTemplateMapping] = fields.field(Mapping)
  def requestBody: StrField            = fields.field(RequestBody)
  def server: Server                   = fields.field(TemplatedLinkModel.Server)

  def withDescription(description: String): this.type          = set(Description, description)
  def withTemplate(template: String): this.type                = set(Template, template)
  def withOperationId(operationId: String): this.type          = set(OperationId, operationId)
  def withOperationRef(operationRef: String): this.type        = set(OperationRef, operationRef)
  def withMapping(mapping: Seq[IriTemplateMapping]): this.type = setArray(Mapping, mapping)
  def withRequestBody(requestBody: String): this.type          = set(RequestBody, requestBody)
  def withServer(server: Server): this.type                    = set(TemplatedLinkModel.Server, server)

  override def meta: Obj            = TemplatedLinkModel
  override def linkCopy(): Linkable = TemplatedLink().withId(id)

  def withIriMapping(variable: String, annotations: Option[Annotations] = None): IriTemplateMapping = {
    val result = annotations.map(a => IriTemplateMapping(a)).getOrElse(IriTemplateMapping())
    result.withTemplateVariable(variable)
    add(Mapping, result)
    result
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    s"/templatedLink/${name.option().getOrElse("UnknownTemplatedLink").urlComponentEncoded}"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = TemplatedLink.apply
  override def nameField: Field                                                                 = Name
}

object TemplatedLink {
  def apply(): TemplatedLink          = apply(Annotations())
  def apply(annotations: Annotations) = new TemplatedLink(Fields(), annotations)
}
