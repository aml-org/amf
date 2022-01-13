package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION}
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import org.mulesoft.antlrast.ast.{ASTElement, Node}

case class GraphQLFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {

  def parse(adopt: Either[PropertyShape, ShapeOperation] => Unit): Unit = {
    arguments() match {
      case args if args.nonEmpty =>
        GraphQLOperationFieldParser(ast).parse((operation) => adopt(Right(operation)))
      case args if args.isEmpty  =>
        GraphQLPropertyFieldParser(ast).parse((property) => adopt(Left(property)))
    }
  }

  private def arguments(): Seq[ASTElement] = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION))

}
