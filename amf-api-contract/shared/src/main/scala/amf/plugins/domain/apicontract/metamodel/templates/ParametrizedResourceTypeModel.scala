package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.plugins.domain.apicontract.models.templates.ParametrizedResourceType

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`
    : List[ValueType] = ApiContract + "ParametrizedResourceType" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedResourceType()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ParametrizedResourceType",
    "RAML resource type that can accept parameters"
  )
}
