package amf.shapes.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.annotations.SourceAST
import amf.core.internal.plugins.document.graph.JsonLdKeywords
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, SemanticContext}
import amf.shapes.internal.domain.metamodel.{ContextMappingModel, NodeShapeModel}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  InvalidCharacteristicsUse,
  UnsupportedContainer
}
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
}

case class SemanticContextResolver(eh: AMFErrorHandler) {

  def computeDocument(jsonDoc: JsonSchemaDocument): Unit = {
    val encodedCtx = computeContext(jsonDoc.encodes, SemanticContext.default)
    jsonDoc.declares.foreach({ case s: Shape => computeContext(s, encodedCtx) })
  }

  def computeContext(
      shape: Shape,
      parentContext: SemanticContext,
      characteristicsAllowed: Boolean = false
  ): SemanticContext = {

    shape match {
      case a: AnyShape => mergeContext(a, parentContext, characteristicsAllowed)
      case _ =>
        computeTree(shape, parentContext)
        parentContext
    }
  }

  private def mergeContext(a: AnyShape, parentContext: SemanticContext, characteristicsAllowed: Boolean): SemanticContext = {
    val context = a.semanticContext.fold(parentContext)(sc =>
      parentContext.merge(semanticContextChecks(sc, characteristicsAllowed))
    )
    a.withSemanticContext(context)
    computeTree(a, context)
    context
  }

  private def semanticContextChecks(baseCtx: SemanticContext, characteristicsAllowed: Boolean): SemanticContext = {
    if (!characteristicsAllowed) cleanOverriddenTerms(baseCtx)
    validateContainerValues(baseCtx)
    baseCtx
  }

  private def cleanOverriddenTerms(context: SemanticContext): Any = {
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

  private def validateContainerValues(context: SemanticContext): Unit = {
    val supportedContainerValues: Seq[String] = Seq(JsonLdKeywords.List)
    context.mapping.foreach { mapping =>
      mapping.container.option() match {
        case Some(container) =>
          if (!supportedContainerValues.contains(container)) {
            eh.violation(
              UnsupportedContainer,
              context.id,
              UnsupportedContainer.message + s". Supported values are: " + supportedContainerValues.mkString(", "),
              context.annotations.find(classOf[SourceAST]).map(_.ast.location).getOrElse(SourceLocation.Unknown)
            )
            mapping.fields.remove(ContextMappingModel.Container.toString)
          }
        case None => // nothing to do
      }
    }
  }

  private def computeTree(shape: Shape, ctx: SemanticContext): Unit = {
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

  private def computeGeneralShapes(others: Iterable[AmfElement], ctx: SemanticContext): Unit = {
    others.toList
      .flatMap({
        case s: Shape             => Some(s)
        case AmfArray(element, _) => element.collect({ case s: Shape => s })
        case _                    => None
      })
      .foreach(computeContext(_, ctx))
  }

  private def computeProperties(element: Seq[PropertyShape], ctx: SemanticContext): Unit = {
    element.foreach { p => computeContext(p.range, ctx, characteristicsAllowed = true) }
  }
}
