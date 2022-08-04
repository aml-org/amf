package amf.shapes.client.scala.model.document

import amf.aml.client.scala.model.document.DialectInstance
import amf.core.client.scala.model.document.Document
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElementModel
import amf.shapes.internal.spec.jsonldschema.instance.model.meta.JsonLDEntityModel

import scala.collection.mutable.ListBuffer

class JsonLDInstanceDocument(fields: Fields, annotations: Annotations, val ctx: EntityContext)
    extends Document(fields, annotations) {}

object JsonLDInstanceDocument {
  def apply(ctx: EntityContext) = new JsonLDInstanceDocument(Fields(), Annotations(), ctx)
}

class EntityContext(val entities: List[JsonLDElementModel]) {}

class EntityContextBuilder() {
  def build(): EntityContext = new EntityContext(entities.toList)

  val entities: ListBuffer[JsonLDElementModel] = ListBuffer()

  def +(jsonLDElementModel: JsonLDElementModel): EntityContextBuilder = {
    entities += jsonLDElementModel
    this
  }
}