package amf.shapes.internal.spec.jsonschema.emitter.document

import amf.core.internal.remote.JsonSchema
import amf.core.internal.render.SpecOrdering
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.common.JSONSchemaVersion
import amf.shapes.internal.spec.common.emitter.JsonSchemaShapeEmitterContext
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaEmitter
import org.yaml.model.YDocument

class JsonSchemaDocumentEmitter(document: JsonSchemaDocument)(implicit val ctx: JsonSchemaShapeEmitterContext) {

  def emit(): YDocument = {

    val baseShape    = document.encodes
    val declarations = document.declares.toList
    val ordering     = SpecOrdering.ordering(JsonSchema, document.sourceSpec)
    val version      = ctx.schemaVersion.asInstanceOf[JSONSchemaVersion]
    new JsonSchemaEmitter(ordering, ctx.config)(ctx.eh).docLikeEmitter(baseShape, declarations, version)

  }

}
