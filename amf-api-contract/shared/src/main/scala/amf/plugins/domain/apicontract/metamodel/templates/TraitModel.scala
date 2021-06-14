package amf.plugins.domain.apicontract.metamodel.templates

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.apicontract.models.templates.Trait

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = ApiContract + "Trait" :: AbstractDeclarationModel.`type`

  override def modelInstance = Trait()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Trait",
    "Type of document base unit encoding a RAML trait"
  )
}
