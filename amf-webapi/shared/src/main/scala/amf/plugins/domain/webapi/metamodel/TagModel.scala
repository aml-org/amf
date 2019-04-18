package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.vocabulary.Namespace.{ApiContract, Core}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.models.Tag

/**
  * Tag meta model
  */
object TagModel extends DomainElementModel with NameFieldSchema with DescriptionField {

  val Documentation = Field(
    CreativeWorkModel,
    Core + "documentation",
    ModelDoc(ModelVocabularies.Core, "documentation", "Documentation about the tag"))

  override val `type`: List[ValueType] = ApiContract + "Tag" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Description,
      Documentation
    ) ++ DomainElementModel.fields

  override def modelInstance = Tag()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Tag",
    "Categorical information provided by some API spec format. Tags are extensions to the model supported directly in the input API spec format."
  )
}
