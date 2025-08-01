package amf.shapes.internal.validation.plugin

import amf.core.client.common.validation.{ProfileName, ProfileNames, ValidationMode}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.plugins.render.JsonLDInstanceRenderHelper

object JsonSchemaBasedSpecValidationHelper {
  private lazy val config = ShapesConfiguration.predefined()

  def validateInstance(
      instanceUnit: JsonLDInstanceDocument,
      schema: Shape,
      profile: ProfileName
  ): AMFValidationReport = {
    val encoded = instanceUnit.encodes.head.asInstanceOf[JsonLDObject]
    val content = JsonLDInstanceRenderHelper.renderToJson(encoded)
    val results = config
      .elementClient()
      .payloadValidatorFor(schema, Mimes.`application/json`, ValidationMode.StrictValidationMode)
      .syncValidate(content)
      .results
      .distinct
    val report = AMFValidationReport(instanceUnit.location().getOrElse(""), profile, results)
    report
  }
}
