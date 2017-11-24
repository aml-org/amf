package amf.plugins.domain.webapi.metamodel.templates

import amf.framework.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.webapi.models.templates.ResourceType
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

object ResourceTypeModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()
}
