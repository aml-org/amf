package amf.plugins.document.vocabularies2.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.document.{BaseUnitModel, ModuleModel}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.metamodel.domain.ExternalModel

object VocabularyModel extends ModuleModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val Base = Field(Str, Namespace.Meta + "base")
  val Imports = Field(Array(VocabularyModel), Namespace.Owl + "imports")
  val Externals = Field(Array(ExternalModel), Namespace.Meta + "externals")

  override val `type`: List[ValueType] =
    Namespace.Owl + "Ontology" :: Namespace.Meta + "Vocabulary" :: BaseUnitModel.`type`

  override def fields: List[Field] = Name :: Imports :: Externals :: Declares :: BaseUnitModel.fields
}
