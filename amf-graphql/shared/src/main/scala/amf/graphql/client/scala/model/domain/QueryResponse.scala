package amf.graphql.client.scala.model.domain

import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.graphql.internal.spec.domain.metamodel.operations.QueryResponseModel
import amf.shapes.client.scala.model.domain.operations.AbstractResponse
import org.yaml.model.YPart

case class QueryResponse(override val fields: Fields, override val annotations: Annotations) extends AbstractResponse {

  override def payload: QueryPayload = fields.field(QueryResponseModel.Payload)

  override def meta: DomainElementModel = QueryResponseModel
}

object QueryResponse {
  def apply(): QueryResponse = apply(Annotations())

  def apply(ast: YPart): QueryResponse = apply(Annotations(ast))

  def apply(annotations: Annotations): QueryResponse = new QueryResponse(Fields(), annotations)
}
