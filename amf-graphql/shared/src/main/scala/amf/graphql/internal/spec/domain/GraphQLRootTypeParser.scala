package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, FIELDS_DEFINITION, FIELD_DEFINITION, INPUT_VALUE_DEFINITION}
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GraphQLRootTypeParser(ast: Node, queryType: RootTypes.Value)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {

  val rootTypeName = findName(ast, "AnonymousType", "", "Missing name for root type")

  def parse(adopt: EndPoint => Unit): Seq[EndPoint] = {
    parseFields(ast, adopt)
  }

  private def parseFields(n: Node, adopt: EndPoint => Unit): Seq[EndPoint] = {
    collect(n, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).map {
      case f: Node =>
        parseField(f, adopt)
    }
  }

  private def path(fieldName: String): String = {
    queryType match {
      case RootTypes.Query => s"/query/$fieldName"
      case RootTypes.Mutation => s"/mutation/$fieldName"
      case RootTypes.Subscription => s"/subscription/$fieldName"
    }
  }

  private def parseField(f: Node, adopt: EndPoint => Unit) = {
    val endPoint: EndPoint = EndPoint(toAnnotations(f))
    val fieldName          = findName(f, "AnonymousField", "", "Missing name for root type field")
    val endpointPath       = path(fieldName)
    endPoint.withPath(endpointPath).withName(s"${rootTypeName}.${fieldName}")
    adopt(endPoint)
    findDescription(f).foreach { description =>
      endPoint.withDescription(cleanDocumentation(description.value))
    }
    parseOperation(f, endPoint, fieldName)
    endPoint
  }

  def parseOperation(f: Node, endPoint: EndPoint, fieldName: String): Unit = {
    val operationId = s"${rootTypeName}.${fieldName}"

    val method = queryType match {
      case RootTypes.Query        => "query"
      case RootTypes.Mutation     => "post"
      case RootTypes.Subscription => "subscribe"
    }

    val op: Operation = endPoint.withOperation(method).withName(operationId).withOperationId(operationId)
    val request       = op.withRequest()
    collect(f, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach {
      case argumentNode: Node =>
        val fieldName =
          findName(argumentNode, "AnonymousArgument", "", s"Missing name for field at root operation $method ")

        val queryParam = request.withQueryParameter(fieldName)

        findDescription(argumentNode) match {
          case Some(t: Terminal) => queryParam.withDescription(cleanDocumentation(t.value))
          case _                 => // ignore
        }

        unpackNilUnion(parseType(argumentNode, queryParam.id)) match {
          case NullableShape(true, shape)  =>
            queryParam.withSchema(shape).withRequired(false)
          case NullableShape(false, shape) =>
            queryParam.withSchema(shape).withRequired(true)
        }
    }

    val payload = op.withResponse("default").withPayload()
    payload.withSchema(parseType(f, payload.id))
  }
}
