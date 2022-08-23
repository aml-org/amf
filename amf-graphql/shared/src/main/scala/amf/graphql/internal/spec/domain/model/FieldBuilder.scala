package amf.graphql.internal.spec.domain.model

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.domain.common.DescriptionField
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.virtual
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes
import amf.graphql.internal.spec.domain.model.FieldBuilderInfo._
import amf.shapes.client.scala.model.domain.AnyShape

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
    annotations: Annotations,
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
    val endpoint     = EndPoint(annotations)
    val endpointPath = EndpointPath(name.toString(), operationType)
    description.foreach(scalar => endpoint.set(DescriptionField.Description, scalar.toString(), scalar.annotations))
    endpoint.withPath(endpointPath).withName(s"$typeName.$name", name.annotations)
    endpoint.withOperations(Seq(operation(endpoint)))
  }

  private def operation(endpoint: EndPoint): Operation = {
    val operationId = endpoint.name.value()
    val method      = OperationMethod(operationType)
    val result      = Operation().withMethod(method).withName(operationId).withOperationId(operationId)
    val request     = result.withRequest()
    if (arguments.nonEmpty) request.withQueryParameters(arguments)
    result.withResponse().withPayload().withSchema(schema)
    result
  }
}

object EmptyScalar extends AmfScalar("", Annotations())
