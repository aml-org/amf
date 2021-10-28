package amf.shapes.internal.spec.jsonschema.semanticjsonschema.dialect

import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class ShapeTransformation(s: AnyShape, ctx: ShapeTransformationContext) {
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

  def ensureNotTransformed(f: => DomainElement): DomainElement = {
    ctx.shapeMap.get(shape.id) match {
      case Some(mapping) => mapping
      case None          => f
    }
  }

  def updateContext(f: ShapeTransformationContext => DomainElement): DomainElement = {
    shape.semanticContext match {
      case Some(semantics) => f(ctx.updateSemanticContext(semantics))
      case _               => f(ctx)
    }
  }
}
