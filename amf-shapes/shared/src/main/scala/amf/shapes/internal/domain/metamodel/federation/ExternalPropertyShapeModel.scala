package amf.shapes.internal.domain.metamodel.federation

import amf.shapes.client.scala.model.domain.federation.ExternalPropertyShape
import amf.core.client.scala.vocabulary.Namespace.{Federation, Shacl}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Str, _}
import amf.core.internal.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}

object ExternalPropertyShapeModel extends DomainElementModel {

  val Name: Field =
    Field(Str, Shacl + "name", ModelDoc(ExternalModelVocabularies.Shacl, "name", "Name for an external data shape"))

  val KeyMappings: Field =
    Field(
      Array(PropertyKeyMappingModel),
      Federation + "keyMappings",
      ModelDoc(
        ModelVocabularies.Federation,
        "keyMappings",
        "Mapping from local properties to properties from the external range shape (identified by rangeName) to be used for data retrieval"
      )
    )

  val RangeName: Field =
    Field(
      Str,
      Federation + "rangeName",
      ModelDoc(
        ModelVocabularies.Federation,
        "rangeName",
        "Federation name of the External Shape in the external graph"
      )
    )

  override def modelInstance: ExternalPropertyShape = ExternalPropertyShape()

  override val `type`: List[ValueType] = Federation + "ExternalPropertyShape" :: DomainElementModel.`type`

  override val fields: List[Field] = Name :: KeyMappings :: RangeName :: DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "ExternalPropertyShape",
    "Model that contains information to locally represent a Property Shape with a Range from an External graph"
  )
}
