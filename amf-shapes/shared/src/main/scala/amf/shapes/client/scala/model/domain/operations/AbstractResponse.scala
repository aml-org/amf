package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.shapes.internal.domain.metamodel.operations.AbstractResponseModel
import amf.shapes.internal.domain.metamodel.operations.AbstractResponseModel._

/** Response internal model.
  */
trait AbstractResponse extends NamedDomainElement {

  type PayloadType <: AbstractPayload

  def payload: PayloadType                         = fields.field(Payload)
  def withPayload(payload: PayloadType): this.type = set(Payload, payload)

  override def meta: AbstractResponseModel = AbstractResponseModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/response"

  override def nameField: Field = Name
}
