package amf.grpc.internal.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.scala.parse.document._
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{IMPORT_STATEMENT, STRING_LITERAL}
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GrpcReferenceHandler() extends ReferenceHandler with GrpcASTParserHelper {
  val collector: CompilerReferenceCollector = CompilerReferenceCollector()

  override def collect(document: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    document match {
      case antlr: AntlrParsedDocument => collectImports(antlr)
      case _                          => collector
    }
  }

  def collectImports(antlr: AntlrParsedDocument): CompilerReferenceCollector = {
    antlr.ast.rootOption().foreach { root =>
      collect(root, Seq(IMPORT_STATEMENT, STRING_LITERAL)).foreach { case stmt: Node =>
        collector.+=(
          stmt.children.head.asInstanceOf[Terminal].value.replaceAll("\"", ""),
          LibraryReference,
          stmt.location
        )
      }
    }
    collector
  }
}
