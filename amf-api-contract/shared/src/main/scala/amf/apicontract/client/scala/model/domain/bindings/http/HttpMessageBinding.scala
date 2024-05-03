package amf.apicontract.client.scala.model.domain.bindings.http
import amf.core.client.scala.model.{IntField, StrField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{
  HttpMessageBinding020Model,
  HttpMessageBinding030Model,
  HttpMessageBindingModel
}
import amf.apicontract.internal.metamodel.domain.bindings.HttpMessageBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingHeaders, BindingVersion, MessageBinding}
import amf.shapes.client.scala.model.domain.Key

abstract class HttpMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with BindingHeaders
    with Key {
  override def key: StrField       = fields.field(HttpMessageBindingModel.key)
  override def componentId: String = "/http-message"

  override protected def bindingVersionField: Field = BindingVersion
  override protected def headersField: Field        = Headers
}

class HttpMessageBinding020(override val fields: Fields, override val annotations: Annotations)
    extends HttpMessageBinding(fields, annotations) {
  override def meta: HttpMessageBinding020Model.type = HttpMessageBinding020Model

  override def componentId: String = "/http-message-020"

  override def linkCopy(): HttpMessageBinding020 = HttpMessageBinding020().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpMessageBinding020.apply
}

object HttpMessageBinding020 {
  def apply(): HttpMessageBinding020 = apply(Annotations())

  def apply(annotations: Annotations): HttpMessageBinding020 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpMessageBinding020 =
    new HttpMessageBinding020(fields, annotations)
}

class HttpMessageBinding030(override val fields: Fields, override val annotations: Annotations)
    extends HttpMessageBinding(fields, annotations) {
  override def meta: HttpMessageBinding030Model.type = HttpMessageBinding030Model

  override def componentId: String = "/http-message-030"

  override def linkCopy(): HttpMessageBinding030 = HttpMessageBinding030().withId(id)

  def statusCode: IntField = fields.field(HttpMessageBinding030Model.StatusCode)
  def withStatusCode(statusCode: Int): this.type = {
    set(HttpMessageBinding030Model.StatusCode, statusCode)
  }

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    HttpMessageBinding030.apply
}

object HttpMessageBinding030 {
  def apply(): HttpMessageBinding030 = apply(Annotations())

  def apply(annotations: Annotations): HttpMessageBinding030 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): HttpMessageBinding030 =
    new HttpMessageBinding030(fields, annotations)
}
