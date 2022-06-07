package amf.graphql.internal.spec.domain

import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{ScalarValueParser, GraphQLASTParserHelper, NullableShape}
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapePayload, ShapeRequest}
import org.mulesoft.antlrast.ast.Node

case class GraphQLOperationFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val operation: ShapeOperation = ShapeOperation(toAnnotations(ast))

  def parse(adopt: ShapeOperation => Unit): Unit = {
    parseName()
    adopt(operation)
    parseDescription()
    parseArguments()
    parseRange()
    GraphQLDirectiveApplicationParser(ast, operation).parse(operation.id)
  }

  private def parseArguments(): Unit = {
    val request = operation.withRequest()
    collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach { case argument: Node =>
      parseArgument(argument, request)
    }
  }

  private def parseArgument(n: Node, request: ShapeRequest) = {
    val name = findName(n, "AnonymousInputType", request.id, "Missing input type name")

    val param = request.withQueryParameter(name).withBinding("query")
    findDescription(n).foreach { desc =>
      param.withDescription(cleanDocumentation(desc.value))
    }

    unpackNilUnion(parseType(n, param.id, _.adopted(param.id))) match {
      case NullableShape(true, shape) =>
        val schema = ScalarValueParser.putDefaultValue(n, shape)
        param.withSchema(schema).withRequired(false)
      case NullableShape(false, shape) =>
        val schema = ScalarValueParser.putDefaultValue(n, shape)
        param.withSchema(schema).withRequired(true)
    }
  }

  private def parseName(): Unit = {
    operation.withName(findName(ast, "AnonymousField", "", "Missing name for field"))
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(t => cleanDocumentation(t.value)).foreach(operation.withDescription)
  }

  private def parseRange(): Unit = {
    val response = operation.withResponse()
    val payload  = ShapePayload().withName("default")
    payload.adopted(response.id).withSchema(parseType(ast, operation.id, _.adopted(payload.id)))
    response.withPayload(payload)
  }
}
