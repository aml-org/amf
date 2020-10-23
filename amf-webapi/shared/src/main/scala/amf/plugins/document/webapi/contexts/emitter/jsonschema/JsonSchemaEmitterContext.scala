package amf.plugins.document.webapi.contexts.emitter.jsonschema

import amf.core.emitter.ShapeRenderOptions
import amf.core.errorhandling.ErrorHandler
import amf.plugins.document.webapi.contexts.emitter.oas.{InlinedJsonSchemaEmitterFactory, Oas2SpecEmitterContext, OasSpecEmitterFactory}
import amf.plugins.document.webapi.parser.spec.declaration.{SchemaVersion, OAS20SchemaVersion}
import amf.plugins.document.webapi.parser.{JsonSchemaTypeDefMatcher, OasTypeDefStringValueMatcher}

import scala.util.matching.Regex

class JsonSchemaEmitterContext(override val eh: ErrorHandler,
                               override val options: ShapeRenderOptions = ShapeRenderOptions(),
                               override val schemaVersion: SchemaVersion)
    extends Oas2SpecEmitterContext(eh = eh, options = options) {
  override val typeDefMatcher: OasTypeDefStringValueMatcher = JsonSchemaTypeDefMatcher

  override val anyOfKey: String                = "anyOf"
  override val nameRegex: Regex                = """^[a-zA-Z0-9\.\-_]+$""".r
  override def schemasDeclarationsPath: String = "/definitions/"
}

object JsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): JsonSchemaEmitterContext =
    new JsonSchemaEmitterContext(eh, options, OAS20SchemaVersion("schema")(eh))
}

final case class InlinedJsonSchemaEmitterContext(override val eh: ErrorHandler,
                                                 override val options: ShapeRenderOptions = ShapeRenderOptions(),
                                                 override val schemaVersion: SchemaVersion)
  extends JsonSchemaEmitterContext(eh = eh, options = options, schemaVersion) {
  override val factory: OasSpecEmitterFactory = InlinedJsonSchemaEmitterFactory()(this)
}

object InlinedJsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): InlinedJsonSchemaEmitterContext =
    InlinedJsonSchemaEmitterContext(eh, options, schemaVersion = OAS20SchemaVersion("schema")(eh))
}