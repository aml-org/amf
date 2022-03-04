package amf.jsonldschema.client.scala.model

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.internal.annotations.Declares
import amf.core.internal.metamodel.document.DocumentModel.References
import amf.core.internal.metamodel.document.{BaseUnitModel, FragmentModel}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.jsonldschema.internal.scala.model.metamodel.JsonLdSchemaEncodesModel
import amf.jsonldschema.internal.scala.model.metamodel.JsonLdSchemaEncodesModel.{Encodes, Declares}

trait JsonLdSchemaEncodesModel extends AmfObject {
  def encodes: Seq[DomainElement]

  def withEncodes(encoded: Seq[DomainElement]): this.type =
    setArrayWithoutId(JsonLdSchemaEncodesModel.Encodes, encoded)
}

case class JsonLdSchemaDocument(fields: Fields, annotations: Annotations)
    extends BaseUnit
    with DeclaresModel
    with JsonLdSchemaEncodesModel {

  /** Meta data for the document */
  override def meta: BaseUnitModel = JsonLdSchemaEncodesModel

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(References)

  /** Declared DomainElements that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields(JsonLdSchemaEncodesModel.Declares)

  override def encodes: Seq[DomainElement] = fields(Encodes)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = ""

}

object JsonLdSchemaDocument {
  def apply() = new JsonLdSchemaDocument(Fields(), Annotations())
}
