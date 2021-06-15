package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.plugins.domain.apicontract.models.IriTemplateMapping

/**
  * Variable metaModel
  */
object IriTemplateMappingModel extends DomainElementModel {

  val TemplateVariable = Field(
    Str,
    ApiContract + "templateVariable",
    ModelDoc(ModelVocabularies.ApiContract, "templateVariable", "Variable defined inside an URL template"))

  val LinkExpression =
    Field(Str,
          ApiContract + "linkExpression",
          ModelDoc(ModelVocabularies.ApiContract, "linkExpression", "OAS 3 link expression"))

  override val `type`: List[ValueType] = ApiContract + "IriTemplateMapping" :: DomainElementModel.`type`

  override def fields: List[Field] = TemplateVariable :: LinkExpression :: DomainElementModel.fields

  override def modelInstance = IriTemplateMapping()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "IriTemplate",
    ""
  )
}
