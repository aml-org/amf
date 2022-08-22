package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDArray(terms: List[String]) extends DomainElement with JsonLDElement {
  override def meta: JsonLDArrayModel = new JsonLDArrayModel(terms)

  /** Set of fields composing object. */
  override val fields: Fields = Fields()

  // TODO native-jsonld: add sourcemaps
  def addMember(value: JsonLDElement): JsonLDArray = {
    add(meta.Members, value)
    this
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = "jsonld-array/"

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.virtual()
}

// TODO native-jsonld: review properties
class JsonLDArrayModel(terms: List[String]) extends JsonLDElementModel {
  override def modelInstance: AmfObject = new JsonLDArray(terms)

  val Members: Field =
    Field(
      Type.Array(JsonLDElementModel),
      Namespace.Data + "members",
      ModelDoc(ModelVocabularies.Data, "members", "members of this array")
    )

  override val `type`: List[ValueType] = terms.map(ValueType.apply) ++ List(Data + "Array")

  override def fields: List[Field] = List(Members)
}
