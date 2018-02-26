package amf.plugins.document.vocabularies2.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.model.domain.PropertyMapping

object PropertyMappingModel extends DomainElementModel {

  val Name                = Field(Str, Namespace.Schema + "name")
  val NodePropertyMapping = Field(Iri, Namespace.Shacl + "path")
  val LiteralRange        = Field(Iri, Namespace.Shacl + "datatype")
  val ObjectRange         = Field(Iri, Namespace.Shacl + "node")

  override def fields: List[Field] = NodePropertyMapping :: Name :: LiteralRange :: ObjectRange :: DomainElementModel.fields

  override def modelInstance: AmfObject = PropertyMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "NodePropertyMapping" :: Namespace.Shacl + "PropertyShape" :: DomainElementModel.`type`
}
