package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.plugins.domain.apicontract.models.templates.ParametrizedTrait

object ParametrizedTraitModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = ApiContract + "ParametrizedTrait" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedTrait()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ParametrizedTrait",
    "RAML trait with declared parameters"
  )
}
