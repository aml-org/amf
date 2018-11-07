package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.{Http, Hydra}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.IriTemplateMapping

/**
  * Variable metaModel
  */
object IriTemplateMappingModel extends DomainElementModel {

  val TemplateVariable = Field(
    Str,
    Hydra + "variable",
    ModelDoc(ExternalModelVocabularies.Hydra, "variable", "Variable defined inside an URL tempplate"))

  val LinkExpression =
    Field(Str, Http + "linkExpression", ModelDoc(ModelVocabularies.Http, "link exprssion", "OAS 3 link expression"))

  override val `type`: List[ValueType] = Hydra + "IriTemplateMapping" :: DomainElementModel.`type`

  override def fields: List[Field] = TemplateVariable :: LinkExpression :: DomainElementModel.fields

  override def modelInstance = IriTemplateMapping()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Iri Template",
    ""
  )
}
