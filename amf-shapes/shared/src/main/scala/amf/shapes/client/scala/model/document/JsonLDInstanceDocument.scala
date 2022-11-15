package amf.shapes.client.scala.model.document

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.context.{EntityContext, SelfContainedContext}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElement
import amf.shapes.internal.document.metamodel.JsonLDInstanceDocumentModel

class JsonLDInstanceDocument(
    override val fields: Fields,
    override val annotations: Annotations,
    override val entityContext: EntityContext
) extends BaseUnit
    with SelfContainedContext {

  /** Meta data for the document */
  override def meta: JsonLDInstanceDocumentModel.type = JsonLDInstanceDocumentModel

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = Nil

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId = ""

  def encodes: Seq[JsonLDElement] = fields(JsonLDInstanceDocumentModel.Encodes)

  def withEncodes(encodes: Seq[JsonLDElement]): this.type = setArray(JsonLDInstanceDocumentModel.Encodes, encodes)
}

object JsonLDInstanceDocument {
  def apply(): JsonLDInstanceDocument = apply(new EntityContext(Nil))
  def apply(ctx: EntityContext)       = new JsonLDInstanceDocument(Fields(), Annotations(), ctx)
}
