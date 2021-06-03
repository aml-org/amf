package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.plugins.domain.apicontract.models.templates.ParametrizedTrait
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = ApiContract + "ParametrizedTrait" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedTrait()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ParametrizedTrait",
    "RAML trait with declared parameters"
  )
}
