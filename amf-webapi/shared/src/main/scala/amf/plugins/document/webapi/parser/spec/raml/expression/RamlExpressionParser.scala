package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.core.annotations.{LexicalInformation, SourceAST, SourceLocation, SourceNode}
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel.UnionShapeModel
import amf.plugins.domain.shapes.models._
import org.yaml.model._

object RamlExpressionParser {

  def parse(adopt: Shape => Unit, expression: String, part: YPart)(implicit ctx: WebApiContext): Option[Shape] = {
    val node        = getValue(part)
    val annotations = Annotations(node).reject(a => a.isInstanceOf[LexicalInformation])
    val position    = Position(node.location.lineFrom, node.location.columnFrom)
    val tokens      = new RamlExpressionLexer(expression, position).lex()
    val builder =
      new RamlExpressionASTBuilder(tokens,
                                   ContextDeclarationFinder(ctx),
                                   annotations,
                                   ContextRegister(ctx, Some(node)))(ctx.eh)
    builder
      .build()
      .map(adoptShapeTree(_, adopt))
      .map(addAnnotations(_, part, expression))
  }

  def parse(adopt: Shape => Unit, expression: String)(implicit ctx: WebApiContext): Option[Shape] = {
    val parser = new RamlExpressionASTBuilder(new RamlExpressionLexer(expression, Position.ZERO).lex(),
                                              ContextDeclarationFinder(ctx),
                                              Annotations(),
                                              EmptyRegister())(ctx.eh)
    parser.build().map(adoptShapeTree(_, adopt))
  }

  def check(adopt: Shape => Unit, expression: String)(implicit ctx: WebApiContext): Option[Shape] = {
    val tokens = new RamlExpressionLexer(expression, Position.ZERO).lex()
    val builder =
      new RamlExpressionASTBuilder(tokens, ContextDeclarationFinder(ctx), unresolvedRegister = EmptyRegister())(ctx.eh)
    builder.build().map(adoptShapeTree(_, adopt))
  }

  private def adoptShapeTree(shape: Shape, adopt: Shape => Unit): Shape = {
    if (shape.isLink) return shape
    adopt(shape)
    shape match {
      case union: UnionShape =>
        val anyOf = union.anyOf.zipWithIndex.map {
          case (shape, index) if !shape.isLink => adoptShapeTree(shape, ParentAdopt(union.id + s"/$index"))
          case (shape, _) if shape.isLink      => adoptShapeTree(shape, DontAdopt())
        }
        union.setArrayWithoutId(UnionShapeModel.AnyOf, anyOf)
      case array: ArrayShape if array.hasItems =>
        val items = adoptShapeTree(array.items, ParentAdopt(array.id))
        array.withItems(items)
      case _ => shape
    }
  }

  private def addAnnotations(shape: Shape, part: YPart, expression: String): Shape = {
    shape.annotations.reject(
      a =>
        a.isInstanceOf[LexicalInformation] || a.isInstanceOf[SourceNode] || a.isInstanceOf[SourceAST] || a
          .isInstanceOf[SourceLocation])
    shape.annotations ++= Annotations(part)
    shape.annotations += ParsedFromTypeExpression(expression)
    shape
  }

  private def getLexical(part: YPart) = {
    LexicalInformation(part.range.lineFrom, part.range.columnFrom, part.range.lineTo, part.range.columnTo)
  }

  private def getValue(ast: YPart): YValue = ast match {
    case e: YMapEntry => e.value.value
    case n: YNode     => n.value
    case s: YScalar   => s
  }

  case class DontAdopt() extends Function[Shape, Unit] {
    override def apply(v1: Shape): Unit = Unit
  }

  case class ParentAdopt(parent: String) extends Function[Shape, Unit] {
    override def apply(shape: Shape): Unit = shape.adopted(parent)
  }
}
