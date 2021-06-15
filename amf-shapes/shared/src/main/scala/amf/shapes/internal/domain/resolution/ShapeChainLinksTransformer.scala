package amf.shapes.internal.domain.resolution

import amf.core.client.scala.model.domain.Shape

class ShapeChainLinksTransformer extends ShapeLinksTransformer {
  override protected def applies(element: Shape): Boolean = true
}
