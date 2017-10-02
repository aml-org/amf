package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Array, Bool, Int, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Shacl, Shapes}
import amf.vocabulary.ValueType

/**
  * Node shape metamodel.
  */
object NodeShapeModel extends ShapeModel with DomainElementModel {

  val MinProperties = Field(Int, Shapes + "minProperties")

  val MaxProperties = Field(Int, Shapes + "maxProperties")

  val Closed = Field(Bool, Shacl + "closed")

  val Discriminator = Field(Str, Shapes + "discriminator")

  val DiscriminatorValue = Field(Str, Shapes + "discriminatorValue")

  val ReadOnly = Field(Bool, Shapes + "readOnly")

  val Properties = Field(Array(PropertyShapeModel), Shacl + "property")

  val Dependencies = Field(Array(PropertyDependenciesModel), Shapes + "dependencies")

  val Inherits = Field(Array(ShapeModel), Shapes + "inherits")

  override def fields: List[Field] =
    List(MinProperties,
         MaxProperties,
         Closed,
         Discriminator,
         DiscriminatorValue,
         ReadOnly,
         Properties,
         Dependencies,
         Inherits) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "NodeShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}
