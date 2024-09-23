package amf.apicontract.internal.spec.avro.validation

import amf.core.client.common.validation._
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.internal.validation.plugin.{AmlAware, BaseModelValidationPlugin}

trait AvroSchemaModelValidationPlugin extends BaseModelValidationPlugin with AmlAware {

  override def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T =
    AvroSchemaModelResolution.withResolvedModel(unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
    )
}
