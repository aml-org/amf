package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.shapes.internal.domain.metamodel.operations.AbstractRequestModel
import amf.shapes.internal.domain.metamodel.operations.AbstractRequestModel._

trait AbstractRequest extends NamedDomainElement {

  type ParameterType <: AbstractParameter

  def queryParameters: Seq[ParameterType] = fields.field(QueryParameters)

  def withQueryParameters(parameters: Seq[ParameterType]): this.type = setArray(QueryParameters, parameters)

  private[amf] def buildQueryParameter: ParameterType

  def withQueryParameter(name: String): ParameterType = {
    val result = buildQueryParameter.withName(name)
    add(QueryParameters, result)
    result
  }

  override def meta: AbstractRequestModel = AbstractRequestModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  def componentId: String = "/request"

  override def nameField: Field = Name
}
