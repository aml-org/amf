package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.core.vocabulary.Namespace.{Shacl, Shapes, Document}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel

/**
  * Node shape metaModel.
  */
object NodeShapeModel extends AnyShapeModel {

  val MinProperties: Field = Field(
    Int,
    Shapes + "minProperties",
    ModelDoc(ModelVocabularies.Shapes, "minProperties", "Minimum number of properties in the input node constraint"))

  val MaxProperties: Field = Field(
    Int,
    Shapes + "maxProperties",
    ModelDoc(ModelVocabularies.Shapes, "maxProperties", "Maximum number of properties in the input node constraint"))

  val Closed: Field = Field(
    Bool,
    Shacl + "closed",
    ModelDoc(ExternalModelVocabularies.Shacl, "closed", "Additional properties in the input node accepted constraint"))

  val AdditionalPropertiesSchema: Field = Field(
    ShapeModel,
    Shacl + "additionalPropertiesSchema",
    ModelDoc(ExternalModelVocabularies.Shacl, "additionalPropertiesSchema", "Additional properties schema"))

  val Discriminator: Field =
    Field(Str, Shapes + "discriminator", ModelDoc(ModelVocabularies.Shapes, "discriminator", "Discriminator property"))

  val DiscriminatorValue: Field = Field(
    Str,
    Shapes + "discriminatorValue",
    ModelDoc(ModelVocabularies.Shapes, "discriminatorValue", "Values for the discriminator property"))

//  @deprecated("Use DiscriminatorValueMapping", "4.7.2")
  val DiscriminatorMapping: Field = Field(
    Array(DiscriminatorValueMappingModel),
    Shapes + "discriminatorMapping",
    ModelDoc(ModelVocabularies.Shapes,
             "discriminatorMapping",
             "Mapping of acceptable values for the node discriminator")
  )

  val DiscriminatorValueMapping: Field = Field(
    Array(DiscriminatorValueMappingModel),
    Shapes + "discriminatorValueMapping",
    ModelDoc(ModelVocabularies.AmlDoc,
             "discriminatorValueMapping",
             "Mapping of acceptable values for the node discriminator")
  )

  val Properties: Field = Field(
    Array(PropertyShapeModel),
    Shacl + "property",
    ModelDoc(ExternalModelVocabularies.Shacl, "property", "Properties associated to this node"))

  val PropertyNames: Field = Field(ShapeModel,
                                   Shacl + "propertyNames",
                                   ModelDoc(ExternalModelVocabularies.Shacl, "propertyNames", "Property names schema"))

  val Dependencies: Field = Field(
    Array(PropertyDependenciesModel),
    Shapes + "dependencies",
    ModelDoc(ModelVocabularies.Shapes, "dependencies", "Dependent properties constraint"))

  val SchemaDependencies: Field = Field(
    Array(SchemaDependenciesModel),
    Shapes + "schemaDependencies",
    ModelDoc(ModelVocabularies.Shapes, "schemaDependencies", "Applied schemas if property exists constraint")
  )

  val UnevaluatedProperties: Field = Field(
    Bool,
    Shapes + "unevaluatedProperties",
    ModelDoc(ModelVocabularies.Shapes,
             "unevaluatedProperties",
             "Accepts that properties may not be evaluated in schema validation")
  )

  val UnevaluatedPropertiesSchema: Field = Field(
    ShapeModel,
    Shapes + "unevaluatedPropertiesSchema",
    ModelDoc(ModelVocabularies.Shapes,
             "unevaluatedPropertiesSchema",
             "Properties that may not be evaluated in schema validation")
  )

  val specificFields = List(
    MinProperties,
    MaxProperties,
    Closed,
    AdditionalPropertiesSchema,
    Discriminator,
    DiscriminatorValue,
    DiscriminatorMapping,
    DiscriminatorValueMapping,
    Properties,
    PropertyNames,
    Dependencies,
    SchemaDependencies,
    UnevaluatedProperties,
    UnevaluatedPropertiesSchema
  )

  override val fields: List[Field] =
    specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "NodeShape") ++ AnyShapeModel.`type`

  override def modelInstance: AnyShape = NodeShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "NodeShape",
    "Shape that validates a record of fields, like a JS object"
  )
}
