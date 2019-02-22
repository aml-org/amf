package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

/**
  * Extension to the model being parsed from RAML annotation or OpenAPI extensions.
  * They must be a DomainPropertySchema (only in RAML) defining them.
  * The DomainPropertySchema might have an associated Data Shape that must validate
  * the extension nested graph.
  *
  * They are parsed as RDF graphs using a default transformation from a set of nested
  * records into RDF
  */
trait DomainExtensionModel extends DomainElementModel with KeyField {

  val Name = Field(Str, Document + "name", ModelDoc(ModelVocabularies.AmlDoc, "name", "Name of an entity"))
  val DefinedBy = Field(CustomDomainPropertyModel,
                        Document + "definedBy",
                        ModelDoc(ModelVocabularies.AmlDoc, "defined by", "Definition for the extended entity"))
  val Extension = Field(DataNodeModel,
                        Document + "extension",
                        ModelDoc(ModelVocabularies.AmlDoc, "extension", "Data structure associated to the extension"))
  val Element =
    Field(Str, Document + "element", ModelDoc(ModelVocabularies.AmlDoc, "element", "Element being extended"))

  override val key: Field = Name

  override def fields: List[Field] = List(Name, DefinedBy, Extension, Element) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "DomainExtension" :: DomainElementModel.`type`
}

object DomainExtensionModel extends DomainExtensionModel {
  override def modelInstance = DomainExtension()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Domain Extension",
    "Extension to the model being parsed from RAML annotation or OpenAPI extensions\nThey must be a DomainPropertySchema (only in RAML) defining them.\nThe DomainPropertySchema might have an associated Data Shape that must validate the extension nested graph.\nThey are parsed as RDF graphs using a default transformation from a set of nested records into RDF."
  )
}
