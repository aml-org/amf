package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.document.{BaseUnitModel, ModuleModel}
import amf.core.metamodel.domain.{ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.{ExternalModel, VocabularyReferenceModel}
import amf.plugins.document.vocabularies.model.document.Vocabulary

object VocabularyModel extends ModuleModel with ExternalContextModel {

  val Name =
    Field(Str, Namespace.Schema + "name", ModelDoc(ExternalModelVocabularies.SchemaOrg, "name", "name for an entity"))
  val Base = Field(Str,
                   Namespace.Meta + "base",
                   ModelDoc(ModelVocabularies.Meta, "base", "Base URI prefix for definitions in this vocabulary"))
  val Imports = Field(Array(VocabularyReferenceModel),
                      Namespace.Owl + "imports",
                      ModelDoc(ExternalModelVocabularies.Owl, "import", "import relationships between vocabularies"))

  override def modelInstance: AmfObject = Vocabulary()

  override val `type`: List[ValueType] =
    Namespace.Meta + "Vocabulary" :: Namespace.Owl + "Ontology" :: BaseUnitModel.`type`

  override def fields: List[Field] =
    Name :: Imports :: Externals :: Declares :: Base :: BaseUnitModel.Location :: BaseUnitModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Vocabulary",
    "Basic primitives for the declaration of vocabularies."
  )
}
