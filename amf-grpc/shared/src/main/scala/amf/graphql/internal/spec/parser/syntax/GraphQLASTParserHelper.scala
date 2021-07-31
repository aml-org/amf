package amf.graphql.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{DESCRIPTION, NAME, STRING_VALUE}
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}

trait GraphQLASTParserHelper extends AntlrASTParserHelper {

  def findDescription(n: ASTElement): Option[Terminal] = {
    collect(n, Seq(DESCRIPTION, STRING_VALUE)).headOption match {
      case Some(t: Terminal) => Some(t)
      case _                 => None
    }
  }

  def findName(n: Node, default: String, errorId: String, error: String)(implicit ctx: GraphQLWebApiContext): String = {
    find(n, NAME).headOption match {
      case Some(t: Terminal) => t.value
      case _                 =>
        astError(errorId, error, toAnnotations(n))
        default
    }
  }

}
