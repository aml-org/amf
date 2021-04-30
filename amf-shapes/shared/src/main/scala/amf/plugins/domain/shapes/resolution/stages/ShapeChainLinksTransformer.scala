package amf.plugins.domain.shapes.resolution.stages

import amf.core.model.domain.Shape

class ShapeChainLinksTransformer extends ShapeLinksTransformer {
  override protected def applies(element: Shape): Boolean = true
}
