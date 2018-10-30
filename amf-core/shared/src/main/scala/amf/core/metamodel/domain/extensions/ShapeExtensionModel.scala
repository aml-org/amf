package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

object ShapeExtensionModel extends DomainElementModel {
  val DefinedBy = Field(CustomDomainPropertyModel, Document + "definedBy", ModelDoc(ModelVocabularies.AmlDoc, "defined by", "Definition for the extended entity"))
  val Extension = Field(DataNodeModel, Document + "extension", ModelDoc(ModelVocabularies.AmlDoc, "extension", "Data structure associated to the extension"))

  override def fields: List[Field] = List(DefinedBy, Extension) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "ShapeExtension" :: DomainElementModel.`type`

  override def modelInstance = ShapeExtension()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Shape Extension",
    "Custom extensions for a data shape definition inside an API definition"
  )
}
