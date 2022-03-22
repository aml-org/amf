package amf.graphql.internal.spec.domain

import amf.graphql.client.scala.model.domain.{QueryOperation, QueryPayload, QueryRequest}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLOperationFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val operation: QueryOperation = QueryOperation(toAnnotations(ast))

  def parse(adopt: QueryOperation => Unit): Unit = {
    parseName()
    adopt(operation)
    parseDescription()
    parseArguments()
    parseRange()
  }

  private def parseArguments(): Unit = {
    val request = operation.withRequest()
    collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach {
      case argument: Node =>
        parseArgument(argument, request)
    }
  }

  private def parseArgument(n: Node, request: QueryRequest) = {
    val name = findName(n, "AnonymousInputType", request.id, "Missing input type name")

    val param = request.withQueryParameter(name).withBinding("query")
    findDescription(n).foreach { desc =>
      param.withDescription(cleanDocumentation(desc.value))
    }

    unpackNilUnion(parseType(n, param.id)) match {
      case NullableShape(true, shape)  => param.withSchema(shape).withRequired(false)
      case NullableShape(false, shape) => param.withSchema(shape).withRequired(true)
    }
  }

  private def parseName(): Unit = {
    operation.withName(findName(ast, "AnonymousField", "", "Missing name for field"))
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(t => cleanDocumentation(t.value)).foreach(operation.withDescription)
  }

  private def parseRange(): Unit = {
    val response = operation.withResponse("default")
    val payload  = QueryPayload().withName("default")
    payload.adopted(response.id).withSchema(parseType(ast, operation.id))
    response.withPayload(payload)
  }
}
