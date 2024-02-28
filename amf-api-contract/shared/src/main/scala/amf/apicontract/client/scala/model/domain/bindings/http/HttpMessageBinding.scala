package amf.apicontract.client.scala.model.domain.bindings.http
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.HttpMessageBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.HttpMessageBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingHeaders, BindingVersion, MessageBinding}
import amf.shapes.client.scala.model.domain.Key

class HttpMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with BindingHeaders
    with Key {
  override def meta: HttpMessageBindingModel.type = HttpMessageBindingModel

  override def key: StrField = fields.field(HttpMessageBindingModel.key)

  override def componentId: String = "/http-message"

  override protected def bindingVersionField: Field = BindingVersion
  override protected def headersField: Field        = Headers

  override def linkCopy(): HttpMessageBinding = HttpMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpMessageBinding.apply
}

object HttpMessageBinding {

  def apply(): HttpMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): HttpMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpMessageBinding = new HttpMessageBinding(fields, annotations)
}
