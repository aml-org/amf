package amf.apicontract.internal.spec.jsonschema

import amf.apicontract.internal.spec.oas.emitter.context.{
  InlinedJsonSchemaEmitterFactory,
  Oas2SpecEmitterContext,
  OasSpecEmitterFactory
}
import amf.core.client.scala.config.ShapeRenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.remote.Vendor
import amf.shapes.internal.spec.common.emitter.JsonSchemaDeclarationsPath
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition, SchemaVersion}

import scala.util.matching.Regex

class JsonSchemaEmitterContext(override val eh: AMFErrorHandler,
                               override val options: ShapeRenderOptions = ShapeRenderOptions(),
                               override val schemaVersion: SchemaVersion)
    extends Oas2SpecEmitterContext(eh = eh, options = options) {

  override val anyOfKey: String = "anyOf"
  override def nameRegex: Regex = """^[a-zA-Z0-9\.\-_]+$""".r

  override val vendor: Vendor = Vendor.JSONSCHEMA

  override def schemasDeclarationsPath: String = JsonSchemaDeclarationsPath(schemaVersion)
}

object JsonSchemaEmitterContext {
  def apply(eh: AMFErrorHandler, options: ShapeRenderOptions): JsonSchemaEmitterContext =
    new JsonSchemaEmitterContext(eh, options, OAS20SchemaVersion(SchemaPosition.Schema))
}

final case class InlinedJsonSchemaEmitterContext(override val eh: AMFErrorHandler,
                                                 override val options: ShapeRenderOptions = ShapeRenderOptions(),
                                                 override val schemaVersion: SchemaVersion)
    extends JsonSchemaEmitterContext(eh = eh, options = options, schemaVersion) {
  override val factory: OasSpecEmitterFactory = InlinedJsonSchemaEmitterFactory()(this)
}

object InlinedJsonSchemaEmitterContext {
  def apply(eh: AMFErrorHandler, options: ShapeRenderOptions): InlinedJsonSchemaEmitterContext =
    InlinedJsonSchemaEmitterContext(eh, options, schemaVersion = OAS20SchemaVersion(SchemaPosition.Schema))
}
