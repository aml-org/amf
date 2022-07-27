package amf.antlr.client.scala.parse.syntax

import amf.core.internal.annotations.SourceAST
import org.mulesoft.antlrast.ast.ASTNode
import org.mulesoft.common.client.lexical.ASTElement

case class SourceASTElement(override val ast: ASTNode) extends SourceAST {
  override type T = ASTNode
}
