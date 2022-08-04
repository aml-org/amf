package amf.shapes.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.client.scala.traversal.iterator.{DomainElementStrategy, IdCollector}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.{AnyShape, SemanticContext}

class ContextTransformationStage extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case jsonDoc: JsonSchemaDocument => computeContext(jsonDoc.encodes, SemanticContext.default)
      case _                           => // ignore
    }
    model
  }

  def computeContext(shape: Shape, parentContext: SemanticContext): Unit = {

    shape match {
      case a: AnyShape =>
        val context = a.semanticContext.fold(parentContext)(sc => parentContext.merge(sc))
        a.withSemanticContext(context)
        computeTree(shape, context)
      case _ =>
        computeTree(shape, parentContext)

    }
  }

  def computeTree(shape: Shape, ctx: SemanticContext) = {

    shape.fields
      .fields()
      .map(_.element)
      .toList
      .flatMap({
        case s: Shape             => Some(s)
        case AmfArray(element, _) => element.collect({ case s: Shape => s })
        case _                    => None
      })
      .foreach { s =>
        computeContext(s, ctx)
      }
  }
}
