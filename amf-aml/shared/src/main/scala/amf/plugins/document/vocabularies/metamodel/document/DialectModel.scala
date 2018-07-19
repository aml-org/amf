package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.document.{DocumentModel, FragmentModel, ModuleModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.{DocumentsModelModel, ExternalModel}
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectFragment, DialectLibrary}

object DialectModel extends DocumentModel with ExternalContextModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val Version = Field(Str, Namespace.Schema + "version")
  val Documents = Field(DocumentsModelModel, Namespace.Meta + "documents")

  override def modelInstance: AmfObject = Dialect()

  override val `type`: List[ValueType] =
    Namespace.Meta + "Dialect" :: DocumentModel.`type`

  override def fields: List[Field] = Name :: Version :: Externals :: Documents :: DocumentModel.fields
}


object DialectLibraryModel extends ModuleModel with ExternalContextModel {
  override def modelInstance: AmfObject = DialectLibrary()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectLibrary" :: DocumentModel.`type`

  override def fields: List[Field] = Externals :: Location :: ModuleModel.fields
}

object DialectFragmentModel extends FragmentModel with ExternalContextModel {
  override def modelInstance: AmfObject = DialectFragment()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectFragment" :: FragmentModel.`type`

  override def fields: List[Field] = Externals :: Location :: FragmentModel.fields
}