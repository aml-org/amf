package amf.apicontract.internal.metamodel.domain.templates

import amf.apicontract.client.scala.model.domain.templates.ParametrizedResourceType
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object ParametrizedResourceTypeModel extends ParametrizedDeclarationModel {
  override val `type`: List[ValueType] = ApiContract + "ParametrizedResourceType" :: ParametrizedDeclarationModel.`type`

  override def modelInstance = ParametrizedResourceType()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ParametrizedResourceType",
    "RAML resource type that can accept parameters"
  )
}
