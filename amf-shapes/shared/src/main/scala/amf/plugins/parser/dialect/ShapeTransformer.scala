package amf.plugins.parser.dialect

import amf.client.model.domain.UnionNodeMapping
import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}

case class ShapeTransformer(s: AnyShape, ctx: ShapeTransformationContext) {
  val shape = s.linkTarget.getOrElse(s).asInstanceOf[AnyShape]

  def transform(): DomainElement = {
    ensureNotTransformed {
      updateContext { ctx =>
        shape match {
          case node: NodeShape if node.properties.nonEmpty => NodeShapeTransformer(node, ctx).transform()
        }
      }
    }
  }

  def ensureNotTransformed(f: => DomainElement): DomainElement = {
    ctx.shapeMap.get(shape.id) match {
      case Some(mapping) => mapping
      case None => f
    }
  }

  def updateContext(f: ShapeTransformationContext => DomainElement): DomainElement = {
    shape.semanticContext match {
      case Some(semantics) => f(ctx.updateSemanticContext(semantics))
      case _            => f(ctx)
    }
  }
}