package amf.antlr.client.scala.parse.syntax

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Annotations
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}

trait AntlrASTParserHelper {
  def find(node: Node, name: String): Seq[ASTElement] = node.children.filter(_.name == name)

  def findAndGetTerminal(node: Node, name: String): Option[Terminal] =
    find(node, name).headOption.flatMap({ case n: Node => n.children.collectFirst({ case t: Terminal => t }) })

  def collect(node: ASTElement, names: Seq[String]): Seq[ASTElement] = {
    if (names.isEmpty) {
      Seq(node)
    } else {
      val nextName = names.head
      node match {
        case n: Node =>
          find(n, nextName).flatMap { nested =>
            collect(nested, names.tail)
          }
        case _ => Nil
      }
    }
  }

  def path(node: ASTElement, names: Seq[String]): Option[ASTElement] = {
    if (names.isEmpty) {
      Some(node)
    } else {
      val nextName = names.head
      node match {
        case n: Node =>
          find(n, nextName) match {
            case found: Seq[ASTElement] if found.length == 1 =>
              path(found.head, names.tail)
            case _ =>
              None
          }
        case _ =>
          None
      }
    }
  }

  def withNode[T](element: ASTElement)(f: Node => T)(implicit ctx: ParserContext): T = element match {
    case node: Node => f(node)
    case _          => throw new Exception(s"Unexpected AST terminal token $element")
  }

  def withOptTerminal[T](element: ASTElement)(f: Option[Terminal] => T)(implicit ctx: ParserContext): T =
    element match {
      case node: Node if node.children.length == 1 && node.children.head.isInstanceOf[Terminal] =>
        f(Some(node.children.head.asInstanceOf[Terminal]))
      case _ =>
        f(None)
    }

  def toAnnotations(elem: ASTElement): Annotations = {
    val lexInfo = LexicalInformation(elem.start.line, elem.start.column, elem.end.line, elem.end.column)
    Annotations() ++= Set(lexInfo)
  }

  def astError(id: String, message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, id, message, annotations)
  }
}
