package amf.graphql.internal.spec.domain

import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedArgument
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape, ScalarValueParser}
import amf.graphql.internal.spec.parser.validation.ParsingValidationsHelper.checkDuplicates
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter, ShapePayload}
import org.mulesoft.antlrast.ast.Node

case class GraphQLOperationFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val operation: ShapeOperation = ShapeOperation(toAnnotations(ast))

  def parse(setterFn: ShapeOperation => Unit): Unit = {
    parseName()
    setterFn(operation)
    parseDescription()
    parseArguments()
    checkArgumentsAreUnique()
    parseRange()
    GraphQLDirectiveApplicationParser(ast, operation).parse()
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
    findDescription(n).foreach { desc =>
      queryParam.withDescription(cleanDocumentation(desc.value))
    }

    unpackNilUnion(parseType(n)) match {
      case NullableShape(true, shape) =>
        val schema = ScalarValueParser.putDefaultValue(n, shape)
        queryParam.withSchema(schema).withRequired(false)
      case NullableShape(false, shape) =>
        val schema = ScalarValueParser.putDefaultValue(n, shape)
        queryParam.withSchema(schema).withRequired(true)
    }
    GraphQLDirectiveApplicationParser(n, queryParam).parse()
    queryParam
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(ast, "AnonymousField", "Missing name for field")
    operation.withName(name, annotations)
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(t => cleanDocumentation(t.value)).foreach(operation.withDescription)
  }

  private def parseRange(): Unit = {
    val response = operation.withResponse()
    val payload  = ShapePayload().withName("default")
    payload.withSchema(parseType(ast))
    response.withPayload(payload)
  }

  private def checkArgumentsAreUnique()(implicit ctx: GraphQLWebApiContext): Unit = {
    val arguments = operation.request.queryParameters
    checkDuplicates(arguments, DuplicatedArgument, duplicatedArgumentMsg)
  }

  private def duplicatedArgumentMsg(argumentName: String): String =
    s"Cannot exist two or more arguments with name '$argumentName'"
}
