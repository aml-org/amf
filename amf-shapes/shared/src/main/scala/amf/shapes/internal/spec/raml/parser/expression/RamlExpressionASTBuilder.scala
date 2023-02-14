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
    unresolvedRegister: UnresolvedRegister
)(implicit val errorHandler: AMFErrorHandler)
    extends AbstractParser
    with AnnotationHelper {

  def build(previous: Option[Shape] = None): Option[Shape] = {
    var result = previous
    if (tokens.isEmpty) return result
    while (!isAtEnd) {
      val current = advance()
      result = current.token match {
        case SYMBOL      => Some(parseSymbol(current))
        case START_ARRAY => Some(parseArray(current, result))
        case UNION       => Some(parseUnion(current, result))
        case START_GROUP => parseGroup(current, result)
        case _           => result
      }
    }
    result
  }

  private def parseGroup(token: Token, previous: Option[Shape]): Option[Shape] = {
    val tokens        = consumeUntil(END_GROUP)
    val maybeEndGroup = consume(END_GROUP)
    val optionalShape = parse(tokens, previous)
    optionalShape.foreach(s => s.annotations += GroupedTypeExpression())
    optionalShape.foreach(s => setShapeAnnotation(s, token, maybeEndGroup))
    optionalShape
  }

  private def parseUnion(token: Token, previous: Option[Shape]): Shape = {
    val union = UnionShape()
    previous match {
      case None =>
        throwError("Syntax error, cannot create empty Union", token)
        union
      case Some(previousShape) =>
        val tokens        = consumeToEnd()
        val optionalShape = parse(tokens, previous)
        optionalShape match {
          case Some(shape) =>
            val nextAnyOf = calculateAnyOf(previousShape, shape)
            union.setWithoutId(UnionShapeModel.AnyOf, AmfArray(nextAnyOf), Annotations(SingleExpression()))
            setShapeAnnotation(union, previousShape, shape)
          case None => union
        }
    }
  }

  private def parse(tokens: Seq[Token], previous: Option[Shape]): Option[Shape] = {
    new RamlExpressionASTBuilder(tokens, declarationFinder, annotations, unresolvedRegister).build(previous)
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

  private def parseArray(token: Token, previous: Option[Shape]): Shape = {
    val array = ArrayShape()
    previous match {
      case None =>
        throwError("Syntax error, generating empty array", token)
        array
      case Some(previousShape) =>
        val maybeEndToken = consume(END_ARRAY)
        if (maybeEndToken.isEmpty) {
          throwError("Syntax error, expected ]", token)
          return array
        }
        val finalShape = previousShape match {
          case _: ArrayShape =>
            ArrayShape()
              .setWithoutId(ArrayShapeModel.Items, previousShape, Annotations(SingleExpression()))
              .toMatrixShapeWithoutId
          case _ => ArrayShape().setWithoutId(ArrayShapeModel.Items, previousShape, Annotations(SingleExpression()))
        }
        setShapeAnnotation(finalShape, token, maybeEndToken)
    }
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
            Annotations.inferred()
          )
        )
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
