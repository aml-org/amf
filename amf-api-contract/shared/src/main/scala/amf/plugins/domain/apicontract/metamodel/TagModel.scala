package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}

import amf.plugins.domain.apicontract.models.Tag

/**
  * Tag meta model
  */
object TagModel extends DomainElementModel with NameFieldSchema with DescriptionField {

  val Documentation = Field(CreativeWorkModel,
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
