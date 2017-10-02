package amf.metadata.domain.extensions

import amf.metadata.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Document, Http}
import amf.vocabulary.ValueType

/**
  * Extension to the model being parsed from RAML annotation or OpenAPI extensions.
  * They must be a DomainPropertySchema (only in RAML) defining them.
  * The DomainPropertySchema might have an associated Data Shape that must validate
  * the extension nested graph.
  *
  * They are parsed as RDF graphs using a default transformation from a set of nested
  * records into RDF
  */
object DomainExtensionModel extends DomainElementModel {

  val DefinedBy = Field(CustomDomainPropertyModel, Document + "definedBy")
  val Extension = Field(DataNodeModel, Document + "extension")

  override def fields: List[Field] = List(DefinedBy, Extension) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "DomainExtension" :: DomainElementModel.`type`
}
