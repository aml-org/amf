package amf.apicontract.internal.spec.jsonschema.emitter.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.common.emitter.OasServersEmitter
import amf.apicontract.internal.spec.oas.emitter.context.{OasSpecEmitterContext, OasSpecEmitterFactory}
import amf.apicontract.internal.spec.oas.emitter.domain.OasSecuritySchemesEmitters
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.{JsonSchema, Spec}
import amf.core.internal.render.SpecOrdering
import amf.shapes.internal.spec.common.SchemaVersion

case class JsonSchemaDocumentEmitterFactory()(implicit val ctx: JsonSchemaDocumentEmitterContext)
    extends OasSpecEmitterFactory {
  override def securitySchemesEmitters(
      securitySchemes: Seq[SecurityScheme],
      ordering: SpecOrdering
  ): OasSecuritySchemesEmitters = ???
  override def serversEmitter(
      api: Api,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter = ???
  override def serversEmitter(
      operation: Operation,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter = ???
  override def serversEmitter(
      endpoint: EndPoint,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter = ???
}

class JsonSchemaDocumentEmitterContext(
    eh: AMFErrorHandler,
    config: RenderConfiguration,
    override val schemaVersion: SchemaVersion
) extends OasSpecEmitterContext(eh = eh, renderConfig = config) {

  override def schemasDeclarationsPath: String = "/definitions/"

  override val factory: OasSpecEmitterFactory = JsonSchemaDocumentEmitterFactory()(this)

  override val spec: Spec = JsonSchema
}
