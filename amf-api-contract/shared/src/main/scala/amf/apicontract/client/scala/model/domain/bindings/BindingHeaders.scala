package amf.apicontract.client.scala.model.domain.bindings
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.metamodel.Field

trait BindingHeaders extends DomainElement {

  protected def headersField: Field

  def headers: Shape = fields.field(headersField)

  def withHeaders(headers: Shape): this.type =
    set(headersField, headers)

}
