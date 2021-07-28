package amf.antlr.client.scala.parse.document

import amf.core.client.scala.parse.document.ParsedDocument
import org.mulesoft.antlrast.ast.AST

case class AntlrParsedDocument(ast: AST, override val comment: Option[String] = None) extends ParsedDocument