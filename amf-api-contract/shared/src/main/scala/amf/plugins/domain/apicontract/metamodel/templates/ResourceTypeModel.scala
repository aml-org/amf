package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.apicontract.models.templates.ResourceType

object ResourceTypeModel extends AbstractDeclarationModel {

  override val `type`: List[ValueType] = ApiContract + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ResourceType",
    "Type of document base unit encoding a RAML resource type"
  )
}
