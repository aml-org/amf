package amf.plugins.document.vocabularies.metamodel.domain
import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Iri, SortedArray, Str}
import amf.core.metamodel.domain._
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.UnionNodeMapping

object UnionNodeMappingModel extends DomainElementModel with LinkableElementModel with MergeableMappingModel with NodeWithDiscriminatorModel with NodeMappableModel {

    val ObjectRange = Field(
    SortedArray(Iri),
    Namespace.Shacl + "node",
    ModelDoc(ExternalModelVocabularies.Shacl, "range", "Object constraint over the type of the mapped property"))


  override def fields: List[Field] = Name :: TypeDiscriminator :: TypeDiscriminatorName :: ObjectRange :: LinkableElementModel.fields ++ DomainElementModel.fields

  override  val `type`: List[ValueType] = Namespace.Meta + "UnionNodeMapping" :: Namespace.Shacl + "Shape" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = UnionNodeMapping()
}
