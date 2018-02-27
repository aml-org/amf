package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel}
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

  val Name      = Field(Str, Document + "name")
  val DefinedBy = Field(CustomDomainPropertyModel, Document + "definedBy")
  val Extension = Field(DataNodeModel, Document + "extension")

  override val key: Field = Name

  override def fields: List[Field] = List(Name, DefinedBy, Extension) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "DomainExtension" :: DomainElementModel.`type`
}

object DomainExtensionModel extends DomainExtensionModel {
  override def modelInstance = DomainExtension()
}
