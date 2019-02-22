package amf.plugins.domain.webapi.metamodel.templates

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.plugins.domain.webapi.models.templates.Trait
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object TraitModel extends AbstractDeclarationModel {
  override val `type`: List[ValueType] = Document + "Trait" :: AbstractDeclarationModel.`type`

  override def modelInstance = Trait()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Trait",
    "Type of document base unit encoding a RAML trait"
  )
}
