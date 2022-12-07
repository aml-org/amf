package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.Request
import amf.apicontract.internal.metamodel.domain.{PayloadModel, RequestModel, ResponseModel}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter, ShapePayload, ShapeRequest}
import amf.shapes.internal.domain.metamodel.operations.{ShapeOperationModel, ShapeParameterModel, ShapeRequestModel}
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
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        ast,
        operation,
        Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(ast, operation, Seq(FIELD_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(ast, operation).parse()
  }

  private def parseArguments(): Unit = {
    val request = ShapeRequest(virtual()).set(RequestModel.Name, "default", synthesized())
    operation.set(ShapeOperationModel.Request, request, inferred())

    val arguments = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map { case argument: Node =>
      parseArgument(argument)
    }
    if (arguments.nonEmpty) request.set(ShapeRequestModel.QueryParameters, AmfArray(arguments, inferred()), inferred())
  }

  private def parseArgument(n: Node): ShapeParameter = {
    val (name, annotations) = findName(n, "AnonymousInputType", "Missing input type name")
    val queryParam = ShapeParameter(toAnnotations(n))
      .withName(name, annotations)
      .set(ShapeParameterModel.Binding, "query", synthesized())
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
      case NullableShape(true, shape) =>
        setDefaultValue(n, queryParam)
        queryParam
          .set(ShapeParameterModel.Schema, shape, inferred())
          .set(ShapeParameterModel.Required, AmfScalar(false, inferred()), inferred())
      case NullableShape(false, shape) =>
        setDefaultValue(n, queryParam)
        queryParam
          .set(ShapeParameterModel.Schema, shape, inferred())
          .set(ShapeParameterModel.Required, AmfScalar(false, inferred()), inferred())
    }
    GraphQLDirectiveApplicationsParser(n, queryParam).parse()
    queryParam
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(ast, "AnonymousField", "Missing name for field")
    operation.withName(name, annotations)
  }

  private def parseRange(): Unit = {
    val response = operation.withResponse()
    response.annotations ++= synthesized()
    val payload = ShapePayload(synthesized())
      .withName("default", synthesized())
      .set(PayloadModel.Schema, parseType(ast), inferred())
    response.set(ResponseModel.Payload, payload, inferred())
  }
}
