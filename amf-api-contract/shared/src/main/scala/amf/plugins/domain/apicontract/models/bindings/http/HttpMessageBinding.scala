package amf.plugins.domain.apicontract.models.bindings.http
import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.HttpMessageBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.HttpMessageBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, MessageBinding}

class HttpMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override def meta: HttpMessageBindingModel.type = HttpMessageBindingModel

  def headers: Shape = fields.field(Headers)

  override def key: StrField = fields.field(HttpMessageBindingModel.key)

  def withHeaders(headers: Shape): this.type = set(Headers, headers)

  override def componentId: String = "/http-message"

  override protected def bindingVersionField: Field = BindingVersion
  override def linkCopy(): HttpMessageBinding       = HttpMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpMessageBinding.apply
}

object HttpMessageBinding {

  def apply(): HttpMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): HttpMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpMessageBinding = new HttpMessageBinding(fields, annotations)
}
