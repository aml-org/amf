package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Any, Bool, Double, Int, Iri, SortedArray, Str}
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.PropertyMapping

object PropertyMappingModel extends DomainElementModel with MergeableMappingModel {

  val Name = Field(
    Str,
    Namespace.Schema + "name",
    ModelDoc(ExternalModelVocabularies.SchemaOrg, "name", "Name in the source AST for the mapped property"))
  val NodePropertyMapping = Field(
    Iri,
    Namespace.Shacl + "path",
    ModelDoc(ExternalModelVocabularies.Shacl, "path", "URI in the mapped graph for this mapped property"))
  val LiteralRange = Field(
    Iri,
    Namespace.Shacl + "datatype",
    ModelDoc(ExternalModelVocabularies.Shacl, "datatype", "Scalar constraint over the type of the mapped property"))
  val ObjectRange = Field(
    SortedArray(Iri),
    Namespace.Shacl + "node",
    ModelDoc(ExternalModelVocabularies.Shacl, "range", "Object constraint over the type of the mapped property"))
  val MapKeyProperty = Field(
    Iri,
    Namespace.Meta + "mapProperty",
    ModelDoc(ModelVocabularies.Meta, "map property", "Marks the mapping as a 'map' mapping syntax"))
  val MapValueProperty = Field(
    Iri,
    Namespace.Meta + "mapValueProperty",
    ModelDoc(ModelVocabularies.Meta, "map value property", "Marks the mapping as a 'map value' mapping syntax"))
  val Sorted = Field(Bool,
                     Namespace.Meta + "sorted",
                     ModelDoc(ModelVocabularies.Meta,
                              "sorted",
                              "Marks the mapping as requiring order in the mapped collection of nodes"))
  val MinCount = Field(
    Int,
    Namespace.Shacl + "minCount",
    ModelDoc(ExternalModelVocabularies.Shacl, "min count", "Minimum count constraint over tha mapped property"))
  val Pattern = Field(
    Str,
    Namespace.Shacl + "pattern",
    ModelDoc(ExternalModelVocabularies.Shacl, "pattern", "Pattern constraint over the mapped property"))
  val Minimum = Field(
    Double,
    Namespace.Shacl + "minInclusive",
    ModelDoc(ExternalModelVocabularies.Shacl, "min inclusive", "Minimum inclusive constraint over the mapped property")
  )
  val Maximum = Field(
    Double,
    Namespace.Shacl + "maxInclusive",
    ModelDoc(ExternalModelVocabularies.Shacl,
             "max inclusive",
             "Maximum inclusive constraint over the mappaed property")
  )
  val AllowMultiple = Field(
    Bool,
    Namespace.Meta + "allowMultiple",
    ModelDoc(ModelVocabularies.Meta, "allow multiple", "Allows multiple mapped nodes for the property mapping"))
  val Enum = Field(
    SortedArray(Any),
    Namespace.Shacl + "in",
    ModelDoc(ExternalModelVocabularies.Shacl, "in", "Enum constraint for the values of the property mapping"))
  val TypeDiscriminator = Field(
    Str,
    Namespace.Meta + "typeDiscriminatorMap",
    ModelDoc(ModelVocabularies.Meta,
             "type discriminator map",
             "Information about the discriminator values in the source AST for the property mapping")
  )
  val TypeDiscriminatorName = Field(
    Str,
    Namespace.Meta + "typeDiscriminatorName",
    ModelDoc(ModelVocabularies.Meta,
             "type discriminator name",
             "Information about the field in the source AST to be used as discrimintaro in the property mapping")
  )
  val Unique = Field(
    Bool,
    Namespace.Meta + "unique",
    ModelDoc(ModelVocabularies.Meta,
             "unique",
             "Marks the values for the property mapping as a primary key for this type of node")
  )

  override def fields: List[Field] =
    NodePropertyMapping :: Name :: LiteralRange :: ObjectRange :: MapKeyProperty ::
      MapValueProperty :: MinCount :: Pattern :: Minimum :: Maximum :: AllowMultiple :: Sorted :: Enum :: TypeDiscriminator ::
      Unique :: TypeDiscriminatorName :: MergePolicy :: DomainElementModel.fields

  override def modelInstance: AmfObject = PropertyMapping()

  override val `type`
    : List[ValueType] = Namespace.Meta + "NodePropertyMapping" :: /* Namespace.Shacl + "PropertyShape" :: */ DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Node Property Mapping",
    "Semantic mapping from an input AST in a dialect document to the output graph of information for a class of output node"
  )
}
