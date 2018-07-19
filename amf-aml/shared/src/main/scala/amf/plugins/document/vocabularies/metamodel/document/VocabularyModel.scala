package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.document.{BaseUnitModel, ModuleModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.{ExternalModel, VocabularyReferenceModel}
import amf.plugins.document.vocabularies.model.document.Vocabulary

object VocabularyModel extends ModuleModel with ExternalContextModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val Base = Field(Str, Namespace.Meta + "base")
  val Imports = Field(Array(VocabularyReferenceModel), Namespace.Owl + "imports")

  override def modelInstance: AmfObject = Vocabulary()

  override val `type`: List[ValueType] =
    Namespace.Owl + "Ontology2" :: Namespace.Meta + "Vocabulary" :: BaseUnitModel.`type`

  override def fields: List[Field] = Name :: Imports :: Externals :: Declares :: Base :: BaseUnitModel.Location :: BaseUnitModel.fields
}
