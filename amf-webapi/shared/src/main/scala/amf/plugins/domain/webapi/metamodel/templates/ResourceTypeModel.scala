package amf.plugins.domain.webapi.metamodel.templates

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.templates.ResourceType

object ResourceTypeModel extends AbstractDeclarationModel {

  override val `type`: List[ValueType] = ApiContract + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Resource Type",
    "Type of document base unit encoding a RAML resource type"
  )
}
