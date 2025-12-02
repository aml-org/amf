package amf.grpc.internal.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.scala.parse.document._
import amf.grpc.internal.spec.common.WellKnownTypes._
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{IMPORT_STATEMENT, STRING_LITERAL}
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GrpcReferenceHandler extends ReferenceHandler with GrpcASTParserHelper {
  val collector: CompilerReferenceCollector = CompilerReferenceCollector()

  override def collect(document: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = document match {
    case antlr: AntlrParsedDocument => collectImports(antlr, ctx)
    case _                          => collector
  }

  private def collectImports(antlr: AntlrParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    antlr.ast.rootOption().foreach { root =>
      collect(root, Seq(IMPORT_STATEMENT, STRING_LITERAL)).foreach { case stmt: Node =>
        val importString =
          stmt.children.headOption.map(_.asInstanceOf[Terminal].value.replaceAll("\"", "")).getOrElse("")
        // Register well-known types in global space
        if (isWellKnownImport(importString)) {
          createShapesForImport(importString, toAnnotations(stmt)).foreach { shape =>
            ctx.globalSpace.update(s".${shape.name.value()}", shape)
          }
        } else {
          collector.+=(
            importString,
            LibraryReference,
            stmt.location
          )
        }
      }
    }
    collector
  }
}
