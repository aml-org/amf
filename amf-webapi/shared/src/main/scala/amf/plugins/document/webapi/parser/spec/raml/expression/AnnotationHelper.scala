package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.core.annotations.LexicalInformation
import amf.core.model.domain.Shape
import amf.core.parser.Annotations

private[expression] trait AnnotationHelper {
  val annotations: Annotations

  protected def fillAnnotationsFor(token: Token) = annotations.copy() += token.lexicalInformation

  protected def fillAnnotationsFor(startToken: Token, endToken: Token) =
    annotations.copy() += computeLexical(startToken, endToken)

  protected def setShapeAnnotation(shape: Shape, startToken: Token, maybeEndToken: Option[Token]) = {
    maybeEndToken.foreach { endToken =>
      shape.annotations ++= fillAnnotationsFor(startToken, endToken)
    }
    shape
  }

  protected def setShapeAnnotation(shapeToAnnotate: Shape, leftShape: Shape, rightShape: Shape): Shape = {
    val leftLexical     = leftShape.annotations.find(classOf[LexicalInformation]).get
    val rightLexical    = rightShape.annotations.find(classOf[LexicalInformation]).get
    val nextAnnotations = annotations.copy() += LexicalInformation(leftLexical.range.start, rightLexical.range.`end`)
    shapeToAnnotate.annotations ++= nextAnnotations
    shapeToAnnotate
  }

  protected def computeLexical(startToken: Token, endToken: Token) = {
    LexicalInformation(startToken.lexicalInformation.range.start, endToken.lexicalInformation.range.end)
  }
}
