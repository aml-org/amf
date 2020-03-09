package amf.plugins.domain.webapi.models.bindings.websockets
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.WebSocketsChannelBindingModel
import amf.plugins.domain.webapi.metamodel.bindings.WebSocketsChannelBindingModel._
import amf.plugins.domain.webapi.models.Key
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, ChannelBinding}

class WebSocketsChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = WebSocketsChannelBindingModel

  def method: StrField = fields.field(Method)
  def query: Shape     = fields.field(Query)
  def headers: Shape   = fields.field(Headers)
  def `type`: StrField = fields.field(Type)

  override def key: StrField = fields.field(WebSocketsChannelBindingModel.key)

  def withMethod(method: String): this.type  = set(Method, method)
  def withQuery(query: Shape): this.type     = set(Query, query)
  def withHeaders(headers: Shape): this.type = set(Headers, headers)

  override def componentId: String                  = "/web-socket-channel"
  override def linkCopy(): WebSocketsChannelBinding = WebSocketsChannelBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    WebSocketsChannelBinding.apply
}

object WebSocketsChannelBinding {

  def apply(): WebSocketsChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): WebSocketsChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): WebSocketsChannelBinding =
    new WebSocketsChannelBinding(fields, annotations)
}
