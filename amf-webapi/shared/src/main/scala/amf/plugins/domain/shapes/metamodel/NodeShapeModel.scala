package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.plugins.domain.shapes.models.NodeShape
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel

/**
  * Node shape metaModel.
  */
object NodeShapeModel extends AnyShapeModel {

  val MinProperties = Field(
    Int,
    Shapes + "minProperties",
    ModelDoc(ModelVocabularies.Shapes, "min properties", "Minimum number of properties in the input node constraint"))

  val MaxProperties = Field(
    Int,
    Shapes + "maxProperties",
    ModelDoc(ModelVocabularies.Shapes, "max properties", "Maximum number of properties in the input node constraint"))

  val Closed = Field(
    Bool,
    Shacl + "closed",
    ModelDoc(ExternalModelVocabularies.Shacl, "closed", "Additional properties in the input node accepted constraint"))

  val AdditionalPropertiesSchema = Field(ShapeModel,
                                         Shacl + "additionalPropertiesSchema",
                                         ModelDoc(ExternalModelVocabularies.Shacl, "additional properties schema", ""))

  val Discriminator =
    Field(Str, Shapes + "discriminator", ModelDoc(ModelVocabularies.Shapes, "discriminator", "Discriminator property"))

  val DiscriminatorValue = Field(
    Str,
    Shapes + "discriminatorValue",
    ModelDoc(ModelVocabularies.Shapes, "discriminator value", "Values for the discriminator property"))

  val DiscriminatorMapping = Field(
    Array(IriTemplateMappingModel),
    Shapes + "discriminatorMapping",
    ModelDoc(ModelVocabularies.Shapes,
             "discriminator mapping",
             "Mappping of acceptable values for the ndoe discriminator")
  )

  val Properties = Field(Array(PropertyShapeModel),
                         Shacl + "property",
                         ModelDoc(ExternalModelVocabularies.Shacl, "property", "Properties associated to this node"))

  val Dependencies = Field(Array(PropertyDependenciesModel),
                           Shapes + "dependencies",
                           ModelDoc(ModelVocabularies.Shapes, "dependencies", "Dependent properties constraint"))

  val specificFields = List(MinProperties,
                            MaxProperties,
                            Closed,
                            AdditionalPropertiesSchema,
                            Discriminator,
                            DiscriminatorValue,
                            DiscriminatorMapping,
                            Properties,
                            Dependencies)

  override val fields: List[Field] =
    specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "NodeShape") ++ AnyShapeModel.`type`

  override def modelInstance = NodeShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Node Shape",
    "Shape that validates a record of fields, like a JS object"
  )
}
