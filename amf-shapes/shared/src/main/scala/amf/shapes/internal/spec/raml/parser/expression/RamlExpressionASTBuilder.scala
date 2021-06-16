package amf.shapes.internal.spec.raml.parser.expression

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, Shape}
import amf.core.internal.annotations.{DeclaredElement, VirtualNode}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, UnionShape}
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, ScalarShapeModel, UnionShapeModel}
import amf.shapes.internal.spec.raml.parser.expression.Token._
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidTypeExpression

import scala.collection.mutable

private[expression] class RamlExpressionASTBuilder(
    override val tokens: Seq[Token],
    declarationFinder: DeclarationFinder,
    override val annotations: Annotations = Annotations(),
    unresolvedRegister: UnresolvedRegister)(implicit val errorHandler: AMFErrorHandler)
    extends AbstractParser
    with AnnotationHelper {

  private val parsedShapes = mutable.Stack[Shape]()

  def build(): Option[Shape] = {
    if (tokens.isEmpty) return None
    while (!isAtEnd) {
      val current = advance()
      val maybeShape = current.token match {
        case SYMBOL      => Some(parseSymbol(current))
        case START_ARRAY => Some(parseArray(current))
        case UNION       => Some(parseUnion(current))
        case START_GROUP => parseGroup(current)
        case _           => None
      }
      maybeShape.foreach(parsedShapes.push)
    }
    parsedShapes.headOption
  }

  private def parseGroup(token: Token): Option[Shape] = {
    val tokens        = consumeUntil(END_GROUP)
    val maybeEndGroup = consume(END_GROUP)
    val optionalShape =
      new RamlExpressionASTBuilder(tokens, declarationFinder, annotations, unresolvedRegister).build()
    optionalShape.foreach(s => s.annotations += GroupedTypeExpression())
    optionalShape.foreach(s => setShapeAnnotation(s, token, maybeEndGroup))
    optionalShape
  }

  private def parseUnion(token: Token): Shape = {
    val union = UnionShape()
    if (parsedShapes.isEmpty) {
      throwError("Syntax error, cannot create empty Union", token)
      return union
    }
    val tokens = consumeToEnd()
    val optionalShape =
      new RamlExpressionASTBuilder(tokens, declarationFinder, annotations, unresolvedRegister).build()
    val previousShape = parsedShapes.pop()
    optionalShape match {
      case Some(shape) =>
        val nextAnyOf = calculateAnyOf(previousShape, shape)
        union.setWithoutId(UnionShapeModel.AnyOf, AmfArray(nextAnyOf), Annotations(SingleExpression()))
        setShapeAnnotation(union, previousShape, shape)
      case None => union
    }
  }

  private def calculateAnyOf(previousShape: Shape, nextShape: Shape): Seq[Shape] = {
    val atLeastOneIsDeclaredElement = linksToDeclaredElement(previousShape) || linksToDeclaredElement(nextShape)
    if (atLeastOneIsDeclaredElement) return Seq(previousShape, nextShape)
    (previousShape, nextShape) match {
      case (previousUnion: UnionShape, nextUnion: UnionShape) => previousUnion.anyOf ++ nextUnion.anyOf
      case (_, union: UnionShape)                             => Seq(previousShape) ++ union.anyOf
      case (union: UnionShape, _)                             => union.anyOf ++ Seq(nextShape)
      case (_, _)                                             => Seq(previousShape, nextShape)
    }
  }

  private def parseArray(token: Token): Shape = {
    val array = ArrayShape()
    if (parsedShapes.isEmpty) {
      throwError("Syntax error, generating empty array", token)
      return array
    }
    val maybeEndToken = consume(END_ARRAY)
    if (maybeEndToken.isEmpty) {
      throwError("Syntax error, expected ]", token)
      return array
    }
    val previousShape = parsedShapes.pop()
    val finalShape = previousShape match {
      case _: ArrayShape =>
        ArrayShape()
          .setWithoutId(ArrayShapeModel.Items, previousShape, Annotations(SingleExpression()))
          .toMatrixShapeWithoutId
      case _ => ArrayShape().setWithoutId(ArrayShapeModel.Items, previousShape, Annotations(SingleExpression()))
    }
    setShapeAnnotation(finalShape, token, maybeEndToken)
  }

  private def parseSymbol(token: Token): Shape = {
    val annotations = fillAnnotationsFor(token)
    val shape = parseRamlNativeType(token, annotations)
      .orElse(lookupInDeclarations(token, annotations))
      .getOrElse(unresolved(token, annotations))
    shape
  }

  private def parseRamlNativeType(token: Token, annotations: Annotations) = {
    val shape = token.value match {
      case "nil"    => Some(NilShape())
      case "any"    => Some(AnyShape())
      case "file"   => Some(FileShape())
      case "object" => Some(NodeShape())
      case "array"  => Some(ArrayShape())
      case "string" | "integer" | "number" | "boolean" | "datetime" | "datetime-only" | "time-only" | "date-only" =>
        Some(
          ScalarShape(Annotations(SingleExpression())).set(
            ScalarShapeModel.DataType,
            AmfScalar(DataType(token.value), Annotations(SingleExpression())),
            Annotations.inferred()))
      case _ => None
    }
    shape.foreach(x => x.annotations ++= annotations)
    shape
  }

  private def lookupInDeclarations(token: Token, annotations: Annotations): Option[Shape] = {
    val typeName = token.value
    declarationFinder.find(typeName).map { shape =>
      val newShape = shape.link(AmfScalar(typeName), annotations, Annotations.synthesized()).asInstanceOf[Shape]
      newShape
    }
  }

  private def unresolved(token: Token, annotations: Annotations): AnyShape = {
    val shape = UnresolvedShape(token.value, annotations).withName(token.value)
    unresolvedRegister.register(shape)
    shape
  }

  private def throwError(message: String, token: Token): Option[AnyShape] = {
    errorHandler.violation(InvalidTypeExpression, "", message, fillAnnotationsFor(token))
    None
  }

  private def linksToDeclaredElement(shape: Shape) =
    shape.linkTarget.exists(_.annotations.contains(classOf[DeclaredElement]))
}

trait ExpressionMember extends VirtualNode

case class GroupedTypeExpression() extends ExpressionMember
case class SingleExpression()      extends ExpressionMember
