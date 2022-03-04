package amf.jsonldschema.internal.scala.model.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.{BaseUnitModel, ModuleModel}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.Type.Array
import amf.jsonldschema.client.scala.model.JsonLdSchemaDocument

object JsonLdSchemaEncodesModel extends BaseUnitModel with ModuleModel {
  val base = "http://a.ml/vocabularies/jsondlschema#"

  val Encodes: Field = Field(
    Array(DomainElementModel),
    ValueType(base + "encodes"),
    ModelDoc(
      ModelVocabularies.AmlDoc,
      "encodes",
      "The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains.")
  )

  override def modelInstance: AmfObject = JsonLdSchemaDocument()

  override def fields: List[Field] = ModuleModel.fields :+ JsonLdSchemaEncodesModel.Encodes

  override val `type`: List[ValueType] = List(ValueType(base + "JsonLdSchemaDocument")) ++ ModuleModel.`type`

}
