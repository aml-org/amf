package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.validation

import amf.core.internal.metamodel.Field
import amf.shapes.internal.domain.metamodel.NodeShapeModel

trait FieldHelper {
  def fieldEnablesCycles(field: Field): Boolean = {
    field match {
      case NodeShapeModel.AdditionalPropertiesSchema => true
      case _                                         => false
    }
  }
}
