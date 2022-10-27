package amf.graphql.internal.spec.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.core.internal.parser.Root
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.DEFINITION
import org.mulesoft.antlrast.ast.ASTNode

trait GraphQLBasedParsePlugin extends ApiParsePlugin with GraphQLASTParserHelper {
  override def applies(element: Root): Boolean = {
    element.parsed match {
      case antlrDoc: AntlrParsedDocument =>
        isGraphQL(antlrDoc)
      case _ =>
        false
    }
  }

  private def isGraphQL(doc: AntlrParsedDocument): Boolean = doc.ast.rootOption().exists(isRootDefinition)

  private def isRootDefinition(root: ASTNode): Boolean = collect(root, Seq(DEFINITION)).nonEmpty
}
