package amf.plugins.domain.webapi.metamodel.templates

import amf.framework.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.webapi.models.templates.Trait
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "Trait" :: AbstractDeclarationModel.`type`

  override def modelInstance = Trait()
}
