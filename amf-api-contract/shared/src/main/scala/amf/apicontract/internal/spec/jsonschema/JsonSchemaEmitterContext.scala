package amf.apicontract.internal.spec.jsonschema

import amf.apicontract.internal.spec.oas.emitter.context.{
  InlinedJsonSchemaEmitterFactory,
  Oas2SpecEmitterContext,
  OasSpecEmitterFactory
}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.emitter.JsonSchemaDeclarationsPath
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition, SchemaVersion}

import scala.util.matching.Regex

class JsonSchemaEmitterContext(
    override val eh: AMFErrorHandler,
    override val renderConfig: RenderConfiguration,
    override val schemaVersion: SchemaVersion
) extends Oas2SpecEmitterContext(eh = eh, config = renderConfig) {

  override val anyOfKey: String = "anyOf"
  override def nameRegex: Regex = """^[a-zA-Z0-9\.\-_]+$""".r

  override val spec: Spec = Spec.JSONSCHEMA

  override def schemasDeclarationsPath: String = JsonSchemaDeclarationsPath(schemaVersion)
}

object JsonSchemaEmitterContext {
  def apply(eh: AMFErrorHandler, config: RenderConfiguration): JsonSchemaEmitterContext =
    new JsonSchemaEmitterContext(eh, config, OAS20SchemaVersion(SchemaPosition.Schema))
}

final case class InlinedJsonSchemaEmitterContext(
    override val eh: AMFErrorHandler,
    override val renderConfig: RenderConfiguration,
    override val schemaVersion: SchemaVersion
) extends JsonSchemaEmitterContext(eh, renderConfig, schemaVersion) {
  override val factory: OasSpecEmitterFactory = InlinedJsonSchemaEmitterFactory()(this)
}

object InlinedJsonSchemaEmitterContext {
  def apply(eh: AMFErrorHandler, config: RenderConfiguration): InlinedJsonSchemaEmitterContext =
    InlinedJsonSchemaEmitterContext(eh, config, schemaVersion = OAS20SchemaVersion(SchemaPosition.Schema))
}
