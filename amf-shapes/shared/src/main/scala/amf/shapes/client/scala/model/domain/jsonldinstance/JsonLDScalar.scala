package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.{AmfObject, AmfScalar}
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Iri, Str}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDScalar(terms: List[String]) extends JsonLDElement {
  override def meta: JsonLDScalarModel = new JsonLDScalarModel(terms)

  /** Set of fields composing object. */
  override val fields: Fields = Fields()

  // TODO native-jsonld: add sourcemaps
  def withValue(value: Any): JsonLDScalar = {
    set(meta.Value, AmfScalar(value))
    this
  }

  def withDataType(dataType: String): JsonLDScalar.this.type = {
    set(meta.DataType, AmfScalar(dataType), Annotations.synthesized())
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = "jsonld-scalar/"

  /** Set of annotations for element. */
  override val annotations: Annotations = Annotations.virtual()
}

// TODO native-jsonld: review properties
class JsonLDScalarModel(terms: List[String]) extends JsonLDElementModel {
  override def modelInstance: AmfObject = new JsonLDScalar(terms)

  val Value: Field =
    Field(Str, Namespace.Data + "value", ModelDoc(ModelVocabularies.Data, "value", "value for an scalar dynamic node"))

  val DataType: Field =
    Field(
      Iri,
      Namespace.Shacl + "datatype",
      ModelDoc(ModelVocabularies.Data, "dataType", "Data type of value for an scalar dynamic node")
    )

  override val `type`: List[ValueType] = terms.map(ValueType.apply) ++ List(Data + "Scalar")

  override def fields: List[Field] = List(Value, DataType)
}
