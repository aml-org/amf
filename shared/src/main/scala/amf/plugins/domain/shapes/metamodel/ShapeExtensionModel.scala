package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel}
import amf.plugins.domain.shapes.models.ShapeExtension
import amf.plugins.domain.webapi.metamodel.CustomDomainPropertyModel
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

object ShapeExtensionModel extends DomainElementModel {
  val DefinedBy = Field(CustomDomainPropertyModel, Document + "definedBy")
  val Extension = Field(DataNodeModel, Document + "extension")

  override def fields: List[Field] = List(DefinedBy, Extension) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Http + "ShapeExtension" :: DomainElementModel.`type`

  override def modelInstance = ShapeExtension()
}
