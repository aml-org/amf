package amf.metadata.domain.extensions

import amf.framework.metamodel.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Document, Http}
import amf.vocabulary.ValueType

object ShapeExtensionModel extends DomainElementModel {
  val DefinedBy = Field(CustomDomainPropertyModel, Document + "definedBy")
  val Extension = Field(DataNodeModel, Document + "extension")

  override def fields: List[Field] = List(DefinedBy, Extension) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "ShapeExtension" :: DomainElementModel.`type`
}
