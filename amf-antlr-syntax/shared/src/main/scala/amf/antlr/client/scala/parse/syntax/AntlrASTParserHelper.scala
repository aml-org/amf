package amf.antlr.client.scala.parse.syntax

import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.{DeclaredElement, LexicalInformation}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.{BaseUnitModel, ModuleModel}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
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
            case t: Terminal =>
              throw new Exception(s"Reached terminal ${t.name} when collecting nodes with path: ${names.mkString(",")}")
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
            case _ => None
          }
        case _ => None
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

  def extractTerminalValue(astNode: ASTNode)(implicit ctx: ParserContext): Option[String] = astNode match {
    case _: Node =>
      withOptTerminal(astNode) {
        case Some(t) => Some(t.value)
        case _       => None
      }
    case Terminal(_, _, value) => Some(value)
    case _                     => None
  }

  /** adds SourceASTElement and LexicalInformation annotations */
  def toAnnotations(elem: ASTNode): Annotations = {
    val lexInfo = LexicalInformation(elem.location.range)
    Annotations(SourceASTElement(elem)) += lexInfo
  }

  def astError(id: String, message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, id, message, annotations)
  }

  def astError(message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, "", message, annotations)
  }

  def setDeclarations(doc: Document)(implicit ctx: WebApiContext): Unit = {
    val declarations = ctx.declarations.shapes.values.toList ++ ctx.declarations.annotations.values.toList
    declarations.foreach(_.annotations += DeclaredElement())
    doc.setWithoutId(ModuleModel.Declares, AmfArray(declarations, Annotations.virtual()), Annotations.virtual())
  }

  def setProcessingData(doc: Document, spec: Spec): Unit = {
    val processingData = APIContractProcessingData(Annotations.synthesized())
    doc.withProcessingData(processingData.withSourceSpec(spec))
    doc set processingData as BaseUnitModel.ProcessingData
  }

  implicit class AntlrFieldSetter[T <: AmfObject](obj: T) {

    class AntlrModelSetter(element: AmfElement, fieldAnnotations: Annotations) {
      def as(field: Field): T = {
        obj.setWithoutId(field, element, fieldAnnotations)
//        obj.set(field, element, fieldAnnotations)
      }

    }

    private def getAnnotation(element: AmfElement): Annotations =
      if (element.annotations.size > 0) element.annotations else Annotations.synthesized()

    def set(element: AmfElement) = new AntlrModelSetter(element, getAnnotation(element))

    def set(value: Boolean, ann: Annotations): AntlrModelSetter = new AntlrModelSetter(AmfScalar(value, ann), ann)
    def set(value: Boolean): AntlrModelSetter                   = set(value, Annotations.synthesized())

    def set(value: String, ann: Annotations): AntlrModelSetter = new AntlrModelSetter(AmfScalar(value, ann), ann)
    def set(value: String): AntlrModelSetter                   = set(value, Annotations.synthesized())

    def set(value: Int, ann: Annotations): AntlrModelSetter = new AntlrModelSetter(AmfScalar(value, ann), ann)
    def set(value: Int): AntlrModelSetter                   = set(value, Annotations.synthesized())

    def set(values: Seq[AmfElement], ann: Annotations): AntlrModelSetter =
      new AntlrModelSetter(AmfArray(values, ann), ann)

    def set(values: Seq[AmfElement]): AntlrModelSetter = set(values, Annotations.synthesized())
  }
}
