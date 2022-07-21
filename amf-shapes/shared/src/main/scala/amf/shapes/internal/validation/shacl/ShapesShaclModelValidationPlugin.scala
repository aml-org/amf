package amf.shapes.internal.validation.shacl

import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

trait ShapesShaclModelValidationPlugin extends BaseShaclModelValidationPlugin {

  override protected val functions: CustomShaclFunctions = ShapesCustomShaclFunctions.functions

}
