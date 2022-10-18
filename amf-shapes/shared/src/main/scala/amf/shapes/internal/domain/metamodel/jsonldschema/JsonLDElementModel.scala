package amf.shapes.internal.domain.metamodel.jsonldschema

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait JsonLDElementModel extends DomainElementModel {}

object JsonLDElementModel extends JsonLDElementModel {

  override def modelInstance: AmfObject = throw new Exception("JsonLDElement is an abstract class")

  override def fields: List[Field] = Nil

  override val `type`: List[ValueType] = (Document + "JsonLDElement") :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "JsonLDElement",
    "Base class for all the JSON-LD elements"
  )
}
