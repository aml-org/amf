package amf.plugins.domain.webapi.metamodel.templates

import amf.framework.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.plugins.domain.webapi.models.templates.ParametrizedResourceType
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = Document + "ParametrizedResourceType" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedResourceType()
}
