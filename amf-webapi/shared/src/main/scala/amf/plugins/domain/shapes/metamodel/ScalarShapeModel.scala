package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.ScalarShape

/**
  * Scalar shape metamodel
  */
object ScalarShapeModel extends AnyShapeModel with CommonShapeFields {

  val DataType = Field(
    Iri,
    Shacl + "datatype",
    ModelDoc(ExternalModelVocabularies.Shacl, "datatype", "Scalar range constraining this scalar shape"))

  val Encoding = Field(
    Str,
    Shapes + "encoding",
    ModelDoc(ModelVocabularies.Shapes, "encoding", "Describes the contents' value encoding")
  )

  val MediaType = Field(
    Str,
    Shapes + "mediaType",
    ModelDoc(ModelVocabularies.Shapes, "mediaType", "Describes the content's value mediatype")
  )

  val Schema = Field(
    ShapeModel,
    Shapes + "contentSchema",
    ModelDoc(ModelVocabularies.Shapes, "contentSchema", "Describes the content's value structure")
  )

  val specificFields = List(DataType, Encoding, MediaType, Schema)
  override val fields
    : List[Field] = specificFields ++ commonOASFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "ScalarShape") ++ AnyShapeModel.`type`

  override def modelInstance = ScalarShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ScalarShape",
    "Data shape describing a scalar value in the input data model, reified as an scalar node in the mapped graph"
  )
}
