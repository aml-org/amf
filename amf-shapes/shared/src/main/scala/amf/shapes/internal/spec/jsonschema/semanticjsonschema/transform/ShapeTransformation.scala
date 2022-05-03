package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape}
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.UnsupportedConstraint

case class ShapeTransformation(s: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler) {

  val shape: AnyShape = s.linkTarget.getOrElse(s).asInstanceOf[AnyShape]

  def transform(): DomainElement = {
    ensureNotTransformed {
      updateContext { ctx =>
        shape match {
          case _: ScalarShape                 => shapeErrorAndDummyMapping("Scalar at this level is not supported")
          case any: AnyShape if any.isAnyType => shapeErrorAndDummyMapping("Any at this level is not supported")
          case not: AnyShape if not.isNot     => shapeErrorAndDummyMapping("Not is not supported")
          case anyOf: AnyShape if anyOf.isOr  => shapeErrorAndDummyMapping("AnyOf is not supported")
          case node: NodeShape                => NodeShapeTransformer(node, ctx).transform()
          case any: AnyShape if any.isAnd || any.isXOne || any.isConditional =>
            AnyShapeTransformer(any, ctx).transform()
          case _ => shapeErrorAndDummyMapping("Non supported schema type")
        }
      }
    }
  }

  private def ensureNotTransformed(f: => DomainElement): DomainElement = {
    ctx.shapeMap.get(shape.id) match {
      case Some(mapping) => mapping
      case None          => f
    }
  }

  private def updateContext[T](f: ShapeTransformationContext => T): T = {
    shape.semanticContext match {
      case Some(semantics) => f(ctx.updateSemanticContext(semantics))
      case _               => f(ctx)
    }
  }

  private def shapeErrorAndDummyMapping(errorMessage: String): NodeMapping = {
    eh.violation(UnsupportedConstraint, shape.id, errorMessage)
    TransformationHelper.dummyMapping(shape)
  }
}
