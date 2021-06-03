package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.apicontract.models.templates.Trait
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = ApiContract + "Trait" :: AbstractDeclarationModel.`type`

  override def modelInstance = Trait()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Trait",
    "Type of document base unit encoding a RAML trait"
  )
}
