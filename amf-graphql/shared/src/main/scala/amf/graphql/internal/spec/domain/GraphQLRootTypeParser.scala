package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Parameter}
import amf.core.client.scala.model.domain.AmfScalar
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes
import amf.graphql.internal.spec.domain.model.{FieldBuilder, GraphqlArgument, OperationMethod}
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{
  ARGUMENTS_DEFINITION,
  FIELDS_DEFINITION,
  FIELD_DEFINITION,
  INPUT_VALUE_DEFINITION
}
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLRootTypeParser(ast: Node, queryType: RootTypes.Value)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  val (rootTypeName, rootTypeAnnotations) = findName(ast, "AnonymousType", "Missing name for root type")

  def parse(): Seq[EndPoint] = parseFields(ast)

  private def parseFields(n: Node): Seq[EndPoint] = {
    collect(n, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).map { case f: Node =>
      parseField(f)
    }
  }

  private def parseField(f: Node) = {
    val (fieldName, annotations) = findName(f, "AnonymousField", "Missing name for root type field")
    val initialBuilder = parseDescription(f).foldLeft(FieldBuilder.empty(toAnnotations(f))) { (builder, description) =>
      builder.withDescription(description)
    }
    val method = OperationMethod(queryType)
    val endpoint = initialBuilder
      .withName(AmfScalar(fieldName, annotations))
      .withTypeName(rootTypeName)
      .withOperationType(queryType)
      .withArguments(parseArguments(f, method))
      .withSchema(parseType(f))
      .build()
    GraphQLDirectiveApplicationsParser(f, endpoint).parse()
    endpoint
  }

  private def parseArguments(n: Node, method: String): List[Parameter] = {
    collect(n, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map { case argument: Node =>
      parseArgument(method, argument)
    }.toList
  }

  private def parseArgument(method: String, argument: Node): Parameter = {
    val (fieldName, annotations) =
      findName(argument, "AnonymousArgument", s"Missing name for field at root operation $method")

    val queryParam = GraphqlArgument(toAnnotations(argument), AmfScalar(fieldName, annotations))
    parseDescription(argument, queryParam, queryParam.meta)
    unpackNilUnion(parseType(argument)) match {
      case NullableShape(true, shape) =>
        setDefaultValue(argument, queryParam)
        queryParam.withSchema(shape).withRequired(false)
      case NullableShape(false, shape) =>
        setDefaultValue(argument, queryParam)
        queryParam.withSchema(shape).withRequired(true)
    }
    GraphQLDirectiveApplicationsParser(argument, queryParam).parse()
    queryParam
  }
}
