package amf.plugins.document.apicontract.parser.spec.raml.expression

import amf.core.annotations.{LexicalInformation, SourceAST, SourceLocation, SourceNode}
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, UnionShapeModel}
import amf.plugins.domain.shapes.models._
import org.yaml.model._

object RamlExpressionParser {

  def parse(adopt: Shape => Unit, expression: String, part: YPart)(implicit ctx: ShapeParserContext): Option[Shape] = {
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

  def check(adopt: Shape => Unit, expression: String)(implicit ctx: ShapeParserContext): Option[Shape] = {
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
        union.fields.getValueAsOption(UnionShapeModel.AnyOf).foreach { value =>
          union.setWithoutId(UnionShapeModel.AnyOf, AmfArray(anyOf, value.value.annotations), value.annotations)
        }
        union
      case array: ArrayShape if array.hasItems =>
        val items = adoptShapeTree(array.items, ParentAdopt(array.id))
        array.fields.getValueAsOption(ArrayShapeModel.Items).foreach { value =>
          array.setWithoutId(ArrayShapeModel.Items, items, value.annotations)
        }
        array
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
