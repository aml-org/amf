package amf.plugins.domain.webapi.models.bindings.http
import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{Shape, Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.HttpMessageBindingModel
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, MessageBinding}
import amf.plugins.domain.webapi.metamodel.bindings.HttpMessageBindingModel._

class HttpMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion {
  override def meta: Obj = HttpMessageBindingModel

  def headers: Shape = fields.field(Headers)

  def withHeaders(headers: Shape): this.type = set(Headers, headers)

  override def componentId: String = "HttpMessageBinding"

  override protected def bindingVersionField: Field = BindingVersion
  override def linkCopy(): HttpMessageBinding = HttpMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = HttpMessageBinding.apply
}

object HttpMessageBinding {

  def apply(): HttpMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): HttpMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpMessageBinding = new HttpMessageBinding(fields, annotations)
}
