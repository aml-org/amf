package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.Annotations.{synthesized, virtual}
import amf.core.internal.parser.domain.Fields
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.document.GraphQLFieldSetter
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.model.domain.operations.{
  ShapeOperation,
  ShapeParameter,
  ShapePayload,
  ShapeRequest,
  ShapeResponse
}
import amf.shapes.internal.domain.metamodel.operations.{
  AbstractPayloadModel,
  ShapeOperationModel,
  ShapeParameterModel,
  ShapeRequestModel,
  ShapeResponseModel
}
import org.mulesoft.antlrast.ast.Node

case class GraphQLOperationFieldParser(ast: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val operation: ShapeOperation = ShapeOperation(toAnnotations(ast))

  def parse(setterFn: ShapeOperation => Unit): Unit = {
    parseName()
    setterFn(operation)
    parseDescription(ast, operation, operation.meta)
    parseArguments()
    parseRange()
    parseFederationMetadata()
    GraphQLDirectiveApplicationsParser(ast, operation).parse()
  }

  private def parseArguments(): Unit = {
    val request = new ShapeRequest(Fields(), virtual()).withName("default")
    operation set AmfArray(Seq(request), virtual()) as ShapeOperationModel.Request

    val arguments = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map { case argument: Node =>
      parseArgument(argument)
    }
    if (arguments.nonEmpty) request set arguments as ShapeRequestModel.QueryParameters
  }

  private def parseArgument(n: Node): ShapeParameter = {
    val (name, annotations) = findName(n, "AnonymousInputType", "Missing input type name")
    val queryParam          = ShapeParameter(toAnnotations(n)).withName(name, annotations)
    queryParam set "query" as ShapeParameterModel.Binding
    parseDescription(n, queryParam, queryParam.meta)
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        n,
        queryParam,
        Seq(INPUT_VALUE_DIRECTIVE, INPUT_FIELD_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(n, queryParam, Seq(INPUT_VALUE_DIRECTIVE, DIRECTIVE)).parse()
    }
    unpackNilUnion(parseType(n)) match {
      case NullableShape(isNullable: Boolean, shape: AnyShape) =>
        setDefaultValue(n, queryParam)
        queryParam set shape as ShapeParameterModel.Schema
        queryParam set !isNullable as ShapeParameterModel.Required
    }
    GraphQLDirectiveApplicationsParser(n, queryParam).parse()
    queryParam
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(ast, "AnonymousField", "Missing name for field")
    operation.withName(name, annotations)
  }

  private def parseRange(): Unit = {
    val response = ShapeResponse(Fields(), virtual()).withName("default")
    operation set AmfArray(Seq(response), virtual()) as ShapeOperationModel.Responses
    val payload = ShapePayload(virtual()).withName("default", synthesized())
    payload set parseType(ast) as AbstractPayloadModel.Schema
    response set payload as ShapeResponseModel.Payload
  }

  private def parseFederationMetadata(): Unit = {
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        ast,
        operation,
        Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(ast, operation, Seq(FIELD_DIRECTIVE, DIRECTIVE)).parse()
    }
  }
}
