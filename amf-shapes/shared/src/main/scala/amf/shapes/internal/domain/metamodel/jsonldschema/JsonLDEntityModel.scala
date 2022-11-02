package amf.shapes.internal.domain.metamodel.jsonldschema

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.spec.jsonldschema.parser.JsonPath

class JsonLDEntityModel(val terms: List[ValueType], val fields: List[Field], path: JsonPath)
    extends JsonLDElementModel {
  override def modelInstance: AmfObject = new JsonLDObject(Fields(), Annotations(), this, path)

  override val `type`: List[ValueType] = terms ++ List(Document + "JsonLDObject")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "JsonLDEntity",
    "JSON-LD Entity"
  )

}
