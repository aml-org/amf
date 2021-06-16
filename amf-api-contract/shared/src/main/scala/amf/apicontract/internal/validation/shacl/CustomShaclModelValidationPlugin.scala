package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin.standardApiProfiles
import amf.core.internal.plugins.validation.ValidationInfo
import amf.core.internal.validation.core.ShaclValidationOptions
import amf.validation.internal.shacl.ShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

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
