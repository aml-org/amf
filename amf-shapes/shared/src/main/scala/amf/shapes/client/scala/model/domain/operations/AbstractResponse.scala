package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractResponseModel
import org.yaml.model.YMapEntry

/**
  * Response internal model.
  */
trait AbstractResponse extends NamedDomainElement {

  def payload: AbstractPayload                         = fields.field(AbstractResponseModel.Payload)
  def withPayload(payload: AbstractPayload): this.type = set(AbstractResponseModel.Payload, payload)

  override def meta: DomainElementModel = AbstractResponseModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/response"

  override def nameField: Field = AbstractResponseModel.Name
}
