package amf.shapes.client.platform
import amf.core.client.platform.AMFParseResult
import amf.core.client.platform.validation.AMFValidationResult
import amf.shapes.client.platform.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.{JsonLDInstanceResult => InternalJsonLDInstanceResult, JsonLDSchemaResult => InternalJsonLDSchemaResult}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JsonLDSchemaResult(
    private[amf] override val _internal: InternalJsonLDSchemaResult
) extends AMFParseResult(_internal) {
  val jsonDocument: JsonSchemaDocument = _internal.jsonDocument
}
@JSExportAll
class JsonLDInstanceResult(
    private[amf] override val _internal: InternalJsonLDInstanceResult
) extends AMFParseResult(_internal) {
  val instance: JsonLDInstanceDocument = _internal.instance
}
