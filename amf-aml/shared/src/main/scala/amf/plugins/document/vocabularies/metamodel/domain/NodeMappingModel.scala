package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.NodeMapping

object NodeMappingModel extends DomainElementModel with LinkableElementModel with MergeableMappingModel with NodeMappableModel {

  val NodeTypeMapping   = Field(Iri, Namespace.Shacl + "targetClass")
  val PropertiesMapping = Field(Array(PropertyMappingModel), Namespace.Shacl + "property")
  val IdTemplate        = Field(Str, Namespace.Hydra + "template")


  override def fields: List[Field] = NodeTypeMapping :: Name :: PropertiesMapping :: IdTemplate :: MergePolicy :: LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance: AmfObject = NodeMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "NodeMapping" :: Namespace.Shacl + "Shape" :: DomainElementModel.`type`
}
