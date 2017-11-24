package amf.plugins.domain.webapi.metamodel.templates

import amf.framework.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.plugins.domain.webapi.models.templates.ParametrizedTrait
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedTrait" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedTrait()
}
