package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.domain.{AnyShape, SemanticContext}
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDElementBuilder
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.ShapeTransformationContext
import org.yaml.model.{YNode, YValue}

abstract class JsonLDBaseElementParser[T <: JsonLDElementBuilder](node: YValue)(implicit ctx: JsonLDParserContext) {

  def parse(shape: Shape): T = checkConditionals(shape).foldLeft(parseNode(shape))(foldLeft)

  def foldLeft(current: T, other: T): T

  def checkConditionals(shape: Shape): Seq[T] = {

    val conditional = if (shape.isConditional) parseConditional(shape) else None

    val oneOf = if (shape.isXOne) shape.xone.find(isValid(_, node)).map(parse) else None
    (conditional ++ oneOf).toSeq
    // TODO native-jsonld: compute all conditionals (oneOf, anyOf)
//    if (anyShape.isXOne) {
//      anyShape.xone.collectFirst({ case a: AnyShape if isValid(a, map) => a }) match {
//        case Some(nodeShape: NodeShape) =>
//        case Some(anyShape: AnyShape) =>
//      }
//    }
  }

  protected def setClassTerm(builder: JsonLDElementBuilder, semantics: Option[SemanticContext]) =
    builder.classTerms ++= findClassTerm(semantics.getOrElse(SemanticContext.default))

  protected def findClassTerm(ctx: SemanticContext) = ctx.typeMappings.flatMap(_.option()).map(t => ctx.expand(t))

  def parseConditional(shape: Shape): Option[T] = selectConditionalShape(shape).map(parse)

  def unsupported(s: Shape): T

  def parseNode(shape: Shape): T

  def selectConditionalShape(shape: Shape): Option[Shape] =
    if (isValid(shape.ifShape, node)) Option(shape.thenShape) else Option(shape.elseShape)

  def isValid(shape: Shape, node: YValue): Boolean = {
    // TODO [Native-jsonld]: implement new validator, interface or configuration to invoke with boolean validator processor (fail early)
    ShapesConfiguration
      .predefined()
      .elementClient()
      .payloadValidatorFor(shape, Mimes.`application/json`, ValidationMode.ScalarRelaxedValidationMode)
      .syncValidate(node.toString)
      .conforms
  }

  protected def buildEmptyAnyShape(parentCtx: SemanticContext) =
    AnyShape().withSemanticContext(parentCtx.copy().withTypeMappings(Nil))
}
