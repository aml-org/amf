package amf.apicontract.internal.spec.jsonschema.emitter.document

import amf.apicontract.client.scala.model.document.JsonSchemaDocument
import amf.apicontract.internal.spec.jsonschema.JsonSchemaEntry
import amf.apicontract.internal.spec.jsonschema.emitter.context.JsonSchemaDocumentEmitterContext
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.remote.JsonSchema
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.shapes.internal.spec.common.{JSONSchemaUnspecifiedVersion, SchemaVersion}
import amf.shapes.internal.spec.jsonschema.emitter.{JsonSchemaEmitter, JsonSchemaEntryEmitter}
import org.yaml.model.YDocument

class JsonSchemaDocumentEmitter(document: JsonSchemaDocument)(implicit val ctx: JsonSchemaDocumentEmitterContext) {

  def emit(): YDocument = {

    val baseShape    = document.encodes
    val declarations = document.declares.toList
    val ordering     = SpecOrdering.ordering(JsonSchema, document.sourceSpec)
    val version = document.schemaVersion.option().flatMap(JsonSchemaEntry(_)).getOrElse(JSONSchemaUnspecifiedVersion)
    val emitters = new JsonSchemaEmitter(ordering, ctx.renderConfig)(ctx.eh)
      .shapeEmitters(baseShape, baseShape :: declarations, version)
      .toList
    val versionEntry = JsonSchemaEntryEmitter(version)

    YDocument {
      _.obj { b =>
        traverse(ordering.sorted(versionEntry :: emitters), b)
      }
    }
  }

}
