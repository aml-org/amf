package amf.graphql.internal.spec.domain.model

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter, Payload, Request, Response}
import amf.apicontract.internal.metamodel.domain.{
  EndPointModel,
  MessageModel,
  OperationModel,
  RequestModel,
  ResponseModel
}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.common.DescribedElementModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes
import amf.graphql.internal.spec.domain.model.FieldBuilderInfo._
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.operations.AbstractPayloadModel
import amf.graphql.internal.spec.document._

trait FieldBuilderInfo
object FieldBuilderInfo {
  sealed trait Empty         extends FieldBuilderInfo
  sealed trait Name          extends FieldBuilderInfo
  sealed trait TypeName      extends FieldBuilderInfo
  sealed trait OperationType extends FieldBuilderInfo
  sealed trait Schema        extends FieldBuilderInfo

  type Mandatory = Empty with Name with TypeName with OperationType with Schema
}

object FieldBuilder {
  def empty(annotations: Annotations): FieldBuilder[Empty] = FieldBuilder[Empty](annotations)
  def empty(): FieldBuilder[Empty]                         = empty(Annotations.empty)
}

case class FieldBuilder[I <: FieldBuilderInfo](
    endpointAnnotations: Annotations,
    name: AmfScalar = EmptyScalar,
    typeName: String = "",
    description: Option[AmfScalar] = None,
    operationType: RootTypes.Value = RootTypes.Query,
    arguments: List[Parameter] = List.empty,
    schema: AnyShape = AnyShape(virtual())
) {

  def withName(name: AmfScalar): FieldBuilder[I with Name] = {
    copy(name = name)
  }

  def withName(name: String): FieldBuilder[I with Name] = {
    copy(name = AmfScalar(name))
  }

  def withTypeName(name: String): FieldBuilder[I with TypeName] = {
    copy(typeName = name)
  }

  def withOperationType(`type`: RootTypes.Value): FieldBuilder[I with OperationType] = {
    copy(operationType = `type`)
  }

  def withDescription(description: AmfScalar): FieldBuilder[I] = {
    copy(description = Some(description))
  }

  def withArguments(arguments: List[Parameter]): FieldBuilder[I] = {
    copy(arguments = arguments)
  }

  def withSchema(schema: AnyShape): FieldBuilder[I with Schema] = {
    copy(schema = schema)
  }

  def build()(implicit ev: I =:= Mandatory): EndPoint = {
    val endpoint     = EndPoint(endpointAnnotations)
    val endpointPath = EndpointPath(name.toString(), operationType)
    description.foreach(scalar => endpoint set scalar as DescribedElementModel.Description)
    endpoint set AmfScalar(endpointPath, inferred()) as EndPointModel.Path
    endpoint.withName(s"$typeName.$name", name.annotations)
    endpoint set Seq(buildOperation(endpoint)) as EndPointModel.Operations
  }

  private def buildOperation(endpoint: EndPoint): Operation = {
    val operationId = endpoint.name
    val method      = OperationMethod(operationType)

    val operation = Operation(virtual())
    operation synthetically () set method as OperationModel.Method
    operation.withName(operationId.value(), operationId.annotations())
    operation synthetically () set operationId.value() as OperationModel.OperationId

    val request = Request(virtual())
    operation set Seq(request) as OperationModel.Request

    val response = Response(virtual()).withName("default", virtual())
    response synthetically () set "200" as ResponseModel.StatusCode
    operation set Seq(response) as OperationModel.Responses

    val payload = Payload(virtual())
    payload set schema as AbstractPayloadModel.Schema
    response set Seq(payload) as MessageModel.Payloads

    if (arguments.nonEmpty) {
      request set AmfArray(arguments, inferred()) as RequestModel.QueryParameters
    }
    operation
  }
}

object EmptyScalar extends AmfScalar("", Annotations())
