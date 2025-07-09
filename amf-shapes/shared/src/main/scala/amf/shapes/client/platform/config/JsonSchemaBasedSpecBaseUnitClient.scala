package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLBaseUnitClient
import amf.core.client.platform.model.document.BaseUnit
import amf.shapes.client.scala.{JsonSchemaBasedSpecBaseUnitClient => InternalJsonSchemaBasedSpecBaseUnitClient}
import amf.core.client.platform.validation.AMFValidationReport
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JsonSchemaBasedSpecBaseUnitClient private[amf](private val _internal: InternalJsonSchemaBasedSpecBaseUnitClient)
    extends AMLBaseUnitClient(_internal) {

  def syncValidate(baseUnit: BaseUnit): AMFValidationReport = _internal.syncValidate(baseUnit._internal)
}
