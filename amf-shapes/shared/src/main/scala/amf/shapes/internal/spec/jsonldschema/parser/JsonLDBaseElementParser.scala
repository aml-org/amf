package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.domain.{AnyShape, SemanticContext}
import amf.shapes.internal.spec.jsonldschema.parser.builder.JsonLDElementBuilder
import org.yaml.model.YValue

abstract class JsonLDBaseElementParser[T <: JsonLDElementBuilder](node: YValue)(implicit ctx: JsonLDParserContext) {

  def parse(shape: Shape): T = checkConditionals(shape).foldLeft(setCharacteristics(parseNode(shape), shape))(foldLeft)

  def setCharacteristics(topNode: T, shape: Shape): T = {
    shape match {
      case a: AnyShape =>
        a.semanticContext.flatMap(_.overrideMappings.headOption).foreach(ot => topNode.withOverriddenTerm(ot.value()))
      case _ => // ignore
    }
    topNode
  }
  protected def foldLeft(current: T, other: T): T

  private def checkConditionals(shape: Shape): Seq[T] = {

    val conditional = if (shape.isConditional) parseConditional(shape) else None

    val oneOf = if (shape.isXOne) shape.xone.find(isValid(_, node)).map(parse) else None
    (conditional ++ oneOf).toSeq
  }

  protected def findClassTerm(ctx: SemanticContext) = ctx.typeMappings.flatMap(_.option()).map(t => ctx.expand(t))

  private def parseConditional(shape: Shape): Option[T] = selectConditionalShape(shape).map(parse)

  protected def unsupported(s: Shape): T

  protected def parseNode(shape: Shape): T

  private def selectConditionalShape(shape: Shape): Option[Shape] =
    if (isValid(shape.ifShape, node)) Option(shape.thenShape) else Option(shape.elseShape)

  private def isValid(shape: Shape, node: YValue): Boolean = {
    // TODO [Native-jsonld]: implement new validator, interface or configuration to invoke with boolean validator processor (fail early)
    ShapesConfiguration
      .predefined()
      .elementClient()
      .payloadValidatorFor(shape, Mimes.`application/json`, ValidationMode.ScalarRelaxedValidationMode)
      .syncValidate(ctx.yValueCache.get(node))
      .conforms
  }
  protected def buildEmptyAnyShape(parentCtx: SemanticContext): AnyShape =
    AnyShape().withSemanticContext(parentCtx.copy().withTypeMappings(Nil))
}
