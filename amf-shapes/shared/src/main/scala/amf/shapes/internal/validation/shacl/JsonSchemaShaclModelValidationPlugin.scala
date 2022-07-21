package amf.shapes.internal.validation.shacl

import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.ValidationOptions
import amf.shapes.internal.validation.plugin.JsonSchemaModelValidationPlugin

import scala.concurrent.{ExecutionContext, Future}

case class JsonSchemaShaclModelValidationPlugin()
    extends ShapesShaclModelValidationPlugin
    with JsonSchemaModelValidationPlugin {

  override val profile: ProfileName = ProfileNames.JSONSCHEMA

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = Future(validateWithShacl(unit, options: ValidationOptions))

}
