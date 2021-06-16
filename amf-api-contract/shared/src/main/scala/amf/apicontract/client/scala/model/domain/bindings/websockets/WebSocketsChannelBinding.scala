package amf.apicontract.client.scala.model.domain.bindings.websockets
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.WebSocketsChannelBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.WebSocketsChannelBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.transformation.stages.Key

class WebSocketsChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field     = BindingVersion
  override def meta: WebSocketsChannelBindingModel.type = WebSocketsChannelBindingModel

  def method: StrField = fields.field(Method)
  def query: Shape     = fields.field(Query)
  def headers: Shape   = fields.field(Headers)
  def `type`: StrField = fields.field(Type)

  override def key: StrField = fields.field(WebSocketsChannelBindingModel.key)

  def withMethod(method: String): this.type  = set(Method, method)
  def withQuery(query: Shape): this.type     = set(Query, query)
  def withHeaders(headers: Shape): this.type = set(Headers, headers)
  def withType(`type`: String): this.type    = set(Type, `type`)

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
