package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractRequestModel
import org.yaml.model.YPart

trait AbstractRequest extends NamedDomainElement {

  def queryParameters: Seq[AbstractParameter] = fields.field(AbstractRequestModel.QueryParameters)

  def withQueryParameters(parameters: Seq[_ <: AbstractParameter]): this.type =
    setArray(AbstractRequestModel.QueryParameters, parameters)

  protected def buildQueryParameter: AbstractParameter

  def withQueryParameter(name: String): AbstractParameter = {
    val result = buildQueryParameter.withName(name)
    add(AbstractRequestModel.QueryParameters, result)
    result
  }

  override def meta: DomainElementModel = AbstractRequestModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] def componentId: String = "/request"

  override def nameField: Field = AbstractRequestModel.Name
}
