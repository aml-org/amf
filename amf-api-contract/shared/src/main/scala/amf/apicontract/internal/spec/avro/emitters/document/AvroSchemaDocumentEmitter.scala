package amf.apicontract.internal.spec.avro.emitters.document

import amf.apicontract.internal.spec.avro.emitters.context.AvroSpecEmitterContext
import amf.apicontract.internal.spec.avro.emitters.domain.AvroDocumentEmitter
import amf.core.internal.remote.AvroSchema
import amf.core.internal.render.SpecOrdering
import amf.shapes.client.scala.model.document.AvroSchemaDocument
import org.yaml.model.YDocument

class AvroSchemaDocumentEmitter(document: AvroSchemaDocument)(implicit val ctx: AvroSpecEmitterContext) {

  def emit(): YDocument = {
    val baseShape = document.encodes
    val ordering  = SpecOrdering.ordering(AvroSchema, document.sourceSpec)
    new AvroDocumentEmitter(ordering, ctx.config)(ctx.eh).docLikeEmitter(baseShape)
  }
}
