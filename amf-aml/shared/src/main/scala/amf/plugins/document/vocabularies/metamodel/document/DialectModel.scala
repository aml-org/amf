package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.document.{DocumentModel, FragmentModel, ModuleModel}
import amf.core.metamodel.domain.{ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.DocumentsModelModel
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectFragment, DialectLibrary}

object DialectModel extends DocumentModel with ExternalContextModel {

  val Name =
    Field(Str, Namespace.Schema + "name", ModelDoc(ExternalModelVocabularies.SchemaOrg, "name", "Name of the dialect"))
  val Version = Field(Str,
                      Namespace.Schema + "version",
                      ModelDoc(ExternalModelVocabularies.SchemaOrg, "version", "Version of the dialect"))
  val Documents = Field(DocumentsModelModel,
                        Namespace.Meta + "documents",
                        ModelDoc(ModelVocabularies.Meta, "documents", "Document mappint for the the dialect"))

  override def modelInstance: AmfObject = Dialect()

  override val `type`: List[ValueType] =
    Namespace.Meta + "Dialect" :: DocumentModel.`type`

  override def fields: List[Field] = Name :: Version :: Externals :: Documents :: DocumentModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Dialect",
    "Definition of an AML dialect, mapping AST nodes from dialect documents into an output semantic graph"
  )
}

object DialectLibraryModel extends ModuleModel with ExternalContextModel {
  override def modelInstance: AmfObject = DialectLibrary()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectLibrary" :: DocumentModel.`type`

  override def fields: List[Field] = Externals :: Location :: ModuleModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Dialect Library",
    "Library of AML mappings that can be reused in different AML dialects"
  )
}

object DialectFragmentModel extends FragmentModel with ExternalContextModel {
  override def modelInstance: AmfObject = DialectFragment()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectFragment" :: FragmentModel.`type`

  override def fields: List[Field] = Externals :: Location :: FragmentModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Dialect Fragment",
    "AML dialect mapping fragment that can be included in multiple AML dialects"
  )
}
