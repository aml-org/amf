package amf.shapes.internal.spec.jsonldschema.instance.model.domain

import amf.core.client.scala.model.domain.{AmfObject, AmfScalar}
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Iri, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDArray(terms: List[String]) extends JsonLDElement {
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
  override def modelInstance: AmfObject = new JsonLDScalar(terms)

  val Members: Field =
    Field(
      JsonLDElementModel,
      Namespace.Data + "members",
      ModelDoc(ModelVocabularies.Data, "members", "members of this array")
    )

  override val `type`: List[ValueType] = terms.map(ValueType.apply) ++ List(Data + "Array")

  override def fields: List[Field] = List(Members)
}
