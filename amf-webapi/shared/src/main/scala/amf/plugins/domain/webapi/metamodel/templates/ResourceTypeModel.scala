package amf.plugins.domain.webapi.metamodel.templates

import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.templates.ResourceType

object ResourceTypeModel extends AbstractDeclarationModel {

  override val `type`: List[ValueType] = Document + "ResourceType" :: AbstractDeclarationModel.`type`

  override def modelInstance = ResourceType()
}
