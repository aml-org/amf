package amf.plugins.document.vocabularies2.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.metamodel.document.{DocumentModel, FragmentModel, ModuleModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.model.document.{DialectInstance, DialectInstanceFragment, DialectInstanceLibrary}

object DialectInstanceModel extends DocumentModel {

  val DefinedBy = Field(Iri, Namespace.Meta + "definedBy")

  override def modelInstance: AmfObject = DialectInstance()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectInstance" :: DocumentModel.`type`

  override def fields: List[Field] = DefinedBy :: DocumentModel.fields
}

object DialectInstanceLibraryModel extends DocumentModel {

  val DefinedBy = Field(Iri, Namespace.Meta + "definedBy")

  override def modelInstance: AmfObject = DialectInstanceLibrary()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectInstanceLibrary" :: ModuleModel.`type`

  override def fields: List[Field] = DefinedBy :: ModuleModel.fields
}

object DialectInstanceFragmentModel extends DocumentModel {

  val DefinedBy = Field(Iri, Namespace.Meta + "definedBy")

  override def modelInstance: AmfObject = DialectInstanceFragment()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectInstanceFragment" :: FragmentModel.`type`

  override def fields: List[Field] = DefinedBy :: FragmentModel.fields
}
