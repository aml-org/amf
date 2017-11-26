package amf.plugins.domain.webapi.metamodel.templates

import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.webapi.models.templates.ResourceType
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object ResourceTypeModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()
}
