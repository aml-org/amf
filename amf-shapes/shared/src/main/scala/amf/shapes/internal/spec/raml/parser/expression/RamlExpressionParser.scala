package amf.shapes.internal.spec.raml.parser.expression

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.{LexicalInformation, SourceAST, SourceNode}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.annotations.ParsedFromTypeExpression
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model._

object RamlExpressionParser {

  def parse(adopt: Shape => Unit, expression: String, part: YPart)(implicit ctx: ShapeParserContext): Option[Shape] = {
    val node     = getValue(part)
    val position = Position(node.location.lineFrom, node.location.columnFrom)
    val tokens   = new RamlExpressionLexer(expression, position).lex()
    val builder =
      new RamlExpressionASTBuilder(
        tokens,
        ContextDeclarationFinder(ctx),
        annotations(node),
        ContextRegister(ctx, Some(node))
      )(ctx.eh)
    val result = builder
      .build()
      .map(addAnnotations(_, part, expression))
    result.foreach(adopt(_))
    result
  }

  def check(adopt: Shape => Unit, expression: String, part: YPart = YNode.Empty)(implicit
      ctx: ShapeParserContext
  ): Option[Shape] = {
    val tokens = new RamlExpressionLexer(expression, part.location.from).lex()
    val builder =
      new RamlExpressionASTBuilder(
        tokens,
        ContextDeclarationFinder(ctx),
        annotations(getValue(part)),
        unresolvedRegister = EmptyRegister()
      )(ctx.eh)
    val result = builder.build()
    result.foreach(adopt(_))
    result
  }

  private def annotations(node: YValue) = {
    val annotations = Annotations(node).reject(a => a.isInstanceOf[LexicalInformation])
    annotations
  }

  private def addAnnotations(shape: Shape, part: YPart, expression: String): Shape = {
    shape.annotations.reject(a =>
      a.isInstanceOf[LexicalInformation] || a.isInstanceOf[SourceNode] || a.isInstanceOf[SourceAST] || a
        .isInstanceOf[amf.core.internal.annotations.SourceLocation]
    )
    shape.annotations ++= Annotations(part)
    shape.annotations += ParsedFromTypeExpression(expression)
    shape
  }

  private def getValue(ast: YPart): YValue = ast match {
    case e: YMapEntry => e.value.value
    case n: YNode     => n.value
    case s: YScalar   => s
  }
}
