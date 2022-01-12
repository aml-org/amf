package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class ShapeTransformation(s: AnyShape, ctx: ShapeTransformationContext)(implicit errorHandler: AMFErrorHandler) {

  val shape: AnyShape = s.linkTarget.getOrElse(s).asInstanceOf[AnyShape]

  def transform(): DomainElement = {
    ensureNotTransformed {
      updateContext { ctx =>
        shape match {
          case node: NodeShape if node.properties.nonEmpty => NodeShapeTransformer(node, ctx).transform()
          case any: AnyShape                               => AnyShapeTransformer(any, ctx).transform()
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
}
