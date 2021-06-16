package amf.apicontract.internal.validation.shacl

import amf.core.internal.plugins.validation.ValidationInfo
import amf.core.internal.validation.core.ShaclValidationOptions
import amf.plugins.features.validation.shacl.ShaclValidator
import amf.plugins.features.validation.shacl.custom.CustomShaclValidator
import amf.plugins.features.validation.shacl.custom.CustomShaclValidator.CustomShaclFunctions

object CustomShaclModelValidationPlugin {

  protected val id: String                      = this.getClass.getSimpleName
  protected val functions: CustomShaclFunctions = CustomShaclFunctions.functions

  def apply() = new CustomShaclModelValidationPlugin()
}

class CustomShaclModelValidationPlugin extends ShaclValidationPlugin {

  override val id: String = CustomShaclModelValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean =
    super.applies(element) && standardApiProfiles.contains(element.profile)

  override protected def validator(options: ShaclValidationOptions): ShaclValidator =
    new CustomShaclValidator(CustomShaclModelValidationPlugin.functions, options)
}
