package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Shacl, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel

/**
  * Node shape metaModel.
  */
trait NodeShapeModel extends AnyShapeModel {

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

  val AdditionalPropertiesKeySchema: Field = Field(
    ShapeModel,
    Shacl + "additionalPropertiesKeySchema",
    ModelDoc(ExternalModelVocabularies.Shacl, "additionalPropertiesKeySchema", "Additional properties key schema")
  )

  val Discriminator: Field =
    Field(Str, Shapes + "discriminator", ModelDoc(ModelVocabularies.Shapes, "discriminator", "Discriminator property"))

  val DiscriminatorValue: Field = Field(
    Str,
    Shapes + "discriminatorValue",
    ModelDoc(ModelVocabularies.Shapes, "discriminatorValue", "Values for the discriminator property"))

//  @deprecated("Use DiscriminatorValueMapping", "4.7.2")
  val DiscriminatorMapping: Field = Field(
    Array(IriTemplateMappingModel),
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

  val Operations: Field = Field(
    Array(ShapeOperationModel),
    Shapes + "supportedOperation",
    ModelDoc(ModelVocabularies.Shapes, "supportedOperation", "Supported operations for this shape")
  )

  val specificFields = List(
    MinProperties,
    MaxProperties,
    Closed,
    AdditionalPropertiesKeySchema,
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
    UnevaluatedPropertiesSchema,
    Operations
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

object NodeShapeModel extends NodeShapeModel
