package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.plugins.domain.shapes.models.NodeShape
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.metamodel.IriTemplateMappingModel

/**
  * Node shape metaModel.
  */
object NodeShapeModel extends AnyShapeModel {

  val MinProperties = Field(Int, Shapes + "minProperties")

  val MaxProperties = Field(Int, Shapes + "maxProperties")

  val Closed = Field(Bool, Shacl + "closed")

  val AdditionalPropertiesSchema = Field(ShapeModel, Shacl + "additionalPropertiesSchema")

  val Discriminator = Field(Str, Shapes + "discriminator")

  val DiscriminatorValue = Field(Str, Shapes + "discriminatorValue")

  val DiscriminatorMapping = Field(Array(IriTemplateMappingModel), Shapes + "discriminatorMapping")

  val Properties = Field(Array(PropertyShapeModel), Shacl + "property")

  val Dependencies = Field(Array(PropertyDependenciesModel), Shapes + "dependencies")

  val specificFields = List(MinProperties,
                            MaxProperties,
                            Closed,
                            AdditionalPropertiesSchema,
                            Discriminator,
                            DiscriminatorValue,
                            Properties,
                            Dependencies)

  override def fields: List[Field] =
    specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "NodeShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = NodeShape()
}
