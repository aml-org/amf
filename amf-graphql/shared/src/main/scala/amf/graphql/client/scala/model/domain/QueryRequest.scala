package amf.graphql.client.scala.model.domain

import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.graphql.internal.spec.domain.metamodel.operations.QueryRequestModel
import amf.shapes.client.scala.model.domain.operations.{AbstractParameter, AbstractRequest}
import org.yaml.model.YPart

case class QueryRequest(override val fields: Fields, override val annotations: Annotations) extends AbstractRequest {
  override protected def buildQueryParameter: AbstractParameter = QueryParameter()

  override def queryParameters: Seq[QueryParameter] = fields.field(QueryRequestModel.QueryParameters)

//  override def withQueryParameters(parameters: Seq[QueryParameter]): this.type = super.withQueryParameters(parameters)

  override def meta: DomainElementModel = QueryRequestModel
}

object QueryRequest {
  def apply(): QueryRequest = apply(Annotations())

  def apply(ast: YPart): QueryRequest = apply(Annotations(ast))

  def apply(annotations: Annotations): QueryRequest = new QueryRequest(Fields(), annotations)
}
