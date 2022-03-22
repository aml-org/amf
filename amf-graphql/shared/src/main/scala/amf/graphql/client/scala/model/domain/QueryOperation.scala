package amf.graphql.client.scala.model.domain

import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.graphql.internal.spec.domain.metamodel.operations.QueryOperationModel
import amf.shapes.client.scala.model.domain.operations.{AbstractOperation, AbstractRequest, AbstractResponse}
import org.yaml.model.YPart

case class QueryOperation(fields: Fields, annotations: Annotations) extends AbstractOperation(fields, annotations) {
  override protected def buildResponse: AbstractResponse = QueryResponse()

  override protected def buildRequest: AbstractRequest = QueryRequest()

  override def request: QueryRequest   = fields.field(QueryOperationModel.Request)
  override def response: QueryResponse = fields.field(QueryOperationModel.Response)

  def withRequest(request: QueryRequest): this.type    = super.withRequest(request)
  def withResponse(response: QueryResponse): this.type = super.withResponse(response)

  override def withRequest(name: String = "default"): QueryRequest = {
    super.withRequest(name).asInstanceOf[QueryRequest]
  }

  override def withResponse(name: String = "default"): QueryResponse = {
    super.withResponse(name).asInstanceOf[QueryResponse]
  }

  override def meta: DomainElementModel = QueryOperationModel
}

object QueryOperation {
  def apply(): QueryOperation = apply(Annotations())

  def apply(ast: YPart): QueryOperation = apply(Annotations(ast))

  def apply(annotations: Annotations): QueryOperation = new QueryOperation(Fields(), annotations)
}
