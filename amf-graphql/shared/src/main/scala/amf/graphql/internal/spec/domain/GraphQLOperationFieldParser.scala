package amf.graphql.internal.spec.domain

import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import amf.graphqlfederation.internal.spec.domain.ShapeFederationMetadataParser
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter, ShapePayload}
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
      ShapeFederationMetadataParser(ast, operation, Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE)).parse()
      GraphQLDirectiveApplicationsParser(ast, operation, Seq(FIELD_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(ast, operation).parse()
  }

  private def parseArguments(): Unit = {
    val request = operation.withRequest()
    val arguments = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map { case argument: Node =>
      parseArgument(argument)
    }
    if (arguments.nonEmpty) request.withQueryParameters(arguments)
  }

  private def parseArgument(n: Node): ShapeParameter = {
    val (name, annotations) = findName(n, "AnonymousInputType", "Missing input type name")
    val queryParam          = ShapeParameter(toAnnotations(n)).withName(name, annotations).withBinding("query")
    parseDescription(n, queryParam, queryParam.meta)
    inFederation { implicit fCtx =>
      ShapeFederationMetadataParser(n, queryParam, Seq(INPUT_VALUE_DIRECTIVE, INPUT_FIELD_FEDERATION_DIRECTIVE)).parse()
      GraphQLDirectiveApplicationsParser(n, queryParam, Seq(INPUT_VALUE_DIRECTIVE, DIRECTIVE)).parse()
    }
    unpackNilUnion(parseType(n)) match {
      case NullableShape(true, shape) =>
        setDefaultValue(n, queryParam)
        queryParam.withSchema(shape).withRequired(false)
      case NullableShape(false, shape) =>
        setDefaultValue(n, queryParam)
        queryParam.withSchema(shape).withRequired(true)
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
    val payload  = ShapePayload().withName("default")
    payload.withSchema(parseType(ast))
    response.withPayload(payload)
  }
}
