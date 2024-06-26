package amf.antlr.client.scala.parse.syntax

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Annotations
import org.mulesoft.antlrast.ast.{ASTNode, Node, Terminal}
import org.mulesoft.common.client.lexical.ASTElement

trait AntlrASTParserHelper {
  def find(node: Node, name: String): Seq[ASTNode] = node.children.filter(_.name == name)

  def collect(node: ASTNode, names: Seq[String]): Seq[ASTNode] = {
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

  def collectNodes(node: Node, names: Seq[String]): Seq[Node] = {
    if (names.isEmpty) {
      Seq(node)
    } else {
      val nextName = names.head
      node match {
        case n: Node =>
          find(n, nextName).flatMap {
            case nested: Node =>
              collectNodes(nested, names.tail)
            case t: Terminal => throw new Exception(s"Reached terminal ${t.name} when collecting nodes with path: ${names.mkString(",")}")
          }
        case _ => Nil
      }
    }
  }

  def path(node: ASTNode, names: Seq[String]): Option[ASTNode] = {
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

  def pathToTerminal(node: Node, names: Seq[String]): Option[Terminal] = {
    path(node, names) match {
      case Some(t: Terminal) => Some(t)
      case _                 => None
    }
  }

  def pathToNonTerminal(node: Node, names: Seq[String]): Option[Node] = {
    path(node, names) match {
      case Some(node: Node) => Some(node)
      case _                => None
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

  def toAnnotations(elem: ASTNode): Annotations = {
    val lexInfo = LexicalInformation(elem.location.range)
    Annotations(SourceASTElement(elem)) ++= Set(lexInfo)
  }

  def astError(id: String, message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, id, message, annotations)
  }

  def astError(message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, "", message, annotations)
  }
}
