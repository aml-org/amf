package amf.grpc.internal.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.scala.parse.document._
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{IMPORT_STATEMENT, STRING_LITERAL}
import org.mulesoft.antlrast.ast.{Node, Terminal}
import org.mulesoft.lexer.SourceLocation

class GrpcReferenceHandler() extends ReferenceHandler with GrpcASTParserHelper {
  val collector: CompilerReferenceCollector = CompilerReferenceCollector()

  override def collect(document: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    document match {
      case antlr: AntlrParsedDocument => collectImports(antlr, ctx.rootContextDocument)
      case _                          => collector
    }
  }

  def collectImports(antlr: AntlrParsedDocument, doc: String): CompilerReferenceCollector = {
    val root = antlr.ast.root()
    collect(root, Seq(IMPORT_STATEMENT, STRING_LITERAL)).foreach { case stmt: Node =>
      collector.+=(stmt.children.head.asInstanceOf[Terminal].value.replaceAll("\"", ""), LibraryReference, new SourceLocation(stmt.file, 0, 0, stmt.start.line, stmt.start.column, stmt.end.line, stmt.end.column))
    }
    collector
  }
}
