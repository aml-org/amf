package amf.shapes.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.client.scala.traversal.iterator.{DomainElementStrategy, IdCollector}
import amf.core.internal.annotations.SourceAST
import amf.core.internal.parser.domain.{FieldEntry, Value}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, SemanticContext}
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.InvalidCharacteristicsUse
import org.mulesoft.common.client.lexical.SourceLocation

class ContextTransformationStage extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case jsonDoc: JsonSchemaDocument => SemanticContextResolver(errorHandler).computeDocument(jsonDoc)
      case _                           => // ignore
    }
    model
  }

  private case class SemanticContextResolver(eh: AMFErrorHandler) {
    def computeDocument(jsonDoc: JsonSchemaDocument): Unit = {
      val encodedCtx = computeContext(jsonDoc.encodes, SemanticContext.default)
      jsonDoc.declares.foreach({ case s: Shape => computeContext(s, encodedCtx) })
    }

    def computeContext(
        shape: Shape,
        parentContext: SemanticContext,
        characteristecsAllowed: Boolean = false
    ): SemanticContext = {

      shape match {
        case a: AnyShape => mergeContext(a, parentContext, characteristecsAllowed)
        case _ =>
          computeTree(shape, parentContext)
          parentContext
      }
    }

    def mergeContext(a: AnyShape, parentContext: SemanticContext, characteristecsAllowed: Boolean): SemanticContext = {
      val termCheckFN: SemanticContext => SemanticContext = (a: SemanticContext) => {
        if (!characteristecsAllowed) cleanOverridedTerms(a)
        a
      }
      val context = a.semanticContext.fold(parentContext)(sc => parentContext.merge(termCheckFN(sc)))
      a.withSemanticContext(context)
      computeTree(a, context)
      context
    }

    def cleanOverridedTerms(context: SemanticContext): Any = {
      val mappings = context.overrideMappings
      if (mappings.nonEmpty) {
        eh.violation(
          InvalidCharacteristicsUse,
          context.id,
          InvalidCharacteristicsUse.message,
          context.annotations.find(classOf[SourceAST]).map(_.ast.location).getOrElse(SourceLocation.Unknown)
        )
        context.withOverrideMappings(Nil)
      }
    }

    def computeTree(shape: Shape, ctx: SemanticContext): Unit = {
      val (properties, others) = shape.fields
        .fields()
        .partition(_.field == NodeShapeModel.Properties)

      computeGeneralShapes(others.map(_.element), ctx)
      computeProperties(
        properties
          .map(_.element)
          .collectFirst { case arr: AmfArray => arr.values.collect({ case p: PropertyShape => p }) }
          .getOrElse(Nil),
        ctx
      )
    }

    def computeGeneralShapes(others: Iterable[AmfElement], ctx: SemanticContext): Unit = {
      others.toList
        .flatMap({
          case s: Shape             => Some(s)
          case AmfArray(element, _) => element.collect({ case s: Shape => s })
          case _                    => None
        })
        .foreach(computeContext(_, ctx))
    }
    def computeProperties(element: Seq[PropertyShape], ctx: SemanticContext): Unit = {
      element.foreach { p => computeContext(p.range, ctx, characteristecsAllowed = true) }
    }
  }
}
