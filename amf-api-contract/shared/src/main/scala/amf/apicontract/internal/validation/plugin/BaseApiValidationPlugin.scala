package amf.apicontract.internal.validation.plugin

import amf.core.client.common.validation._
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.internal.validation.plugin.{AmlAware, BaseModelValidationPlugin}

trait BaseApiValidationPlugin extends BaseModelValidationPlugin with AmlAware {

  override def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = APIModelResolution.withResolvedModel(unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
    withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  )

}
