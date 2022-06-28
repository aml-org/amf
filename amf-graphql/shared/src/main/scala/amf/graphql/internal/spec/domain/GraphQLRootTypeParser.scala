package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.graphql.internal.spec.parser.syntax.{ScalarValueParser, GraphQLASTParserHelper, NullableShape}
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{
  ARGUMENTS_DEFINITION,
  FIELDS_DEFINITION,
  FIELD_DEFINITION,
  INPUT_VALUE_DEFINITION
}
import amf.shapes.client.scala.model.domain.AnyShape
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GraphQLRootTypeParser(ast: Node, queryType: RootTypes.Value)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {

  val rootTypeName: String = findName(ast, "AnonymousType", "Missing name for root type")

  def parse(setterFn: EndPoint => Unit): Seq[EndPoint] = {
    parseFields(ast, setterFn)
  }

  private def parseFields(n: Node, setterFn: EndPoint => Unit): Seq[EndPoint] = {
    collect(n, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).map { case f: Node =>
      parseField(f, setterFn)
    }
  }

  private def path(fieldName: String): String = {
    queryType match {
      case RootTypes.Query        => s"/query/$fieldName"
      case RootTypes.Mutation     => s"/mutation/$fieldName"
      case RootTypes.Subscription => s"/subscription/$fieldName"
    }
  }

  private def parseField(f: Node, setterFn: EndPoint => Unit) = {
    val endPoint: EndPoint = EndPoint(toAnnotations(f))
    val fieldName          = findName(f, "AnonymousField", "Missing name for root type field")
    val endpointPath       = path(fieldName)
    endPoint.withPath(endpointPath).withName(s"$rootTypeName.$fieldName")
    setterFn(endPoint)
    findDescription(f).foreach { description =>
      endPoint.withDescription(cleanDocumentation(description.value))
    }
    parseOperation(f, endPoint, fieldName)
    GraphQLDirectiveApplicationParser(f, endPoint).parse()
    endPoint
  }

  def parseOperation(f: Node, endPoint: EndPoint, fieldName: String): Unit = {
    val operationId = s"$rootTypeName.$fieldName"

    val method = queryType match {
      case RootTypes.Query        => "query"
      case RootTypes.Mutation     => "post"
      case RootTypes.Subscription => "subscribe"
    }

    val op: Operation = endPoint.withOperation(method).withName(operationId).withOperationId(operationId)
    val request       = op.withRequest()
    collect(f, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach { case argumentNode: Node =>
      val fieldName =
        findName(argumentNode, "AnonymousArgument", s"Missing name for field at root operation $method ")

      val queryParam = request.withQueryParameter(fieldName).withBinding("query")

      findDescription(argumentNode) match {
        case Some(t: Terminal) => queryParam.withDescription(cleanDocumentation(t.value))
        case _                 => // ignore
      }

      unpackNilUnion(parseType(argumentNode)) match {
        case NullableShape(true, shape) =>
          val schema = ScalarValueParser.putDefaultValue(ast, shape)
          queryParam.withSchema(schema).withRequired(false)
        case NullableShape(false, shape) =>
          val schema = ScalarValueParser.putDefaultValue(ast, shape)
          queryParam.withSchema(schema).withRequired(true)
      }
    }

    val payload                 = op.withResponse().withPayload()
    val shape                   = parseType(f)
    payload.withSchema(shape)
  }
}
