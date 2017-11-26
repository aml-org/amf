package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.NodeShape
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Node shape metamodel.
  */
object NodeShapeModel extends AnyShapeModel with DomainElementModel {

  val MinProperties = Field(Int, Shapes + "minProperties")

  val MaxProperties = Field(Int, Shapes + "maxProperties")

  val Closed = Field(Bool, Shacl + "closed")

  val Discriminator = Field(Str, Shapes + "discriminator")

  val DiscriminatorValue = Field(Str, Shapes + "discriminatorValue")

  val ReadOnly = Field(Bool, Shapes + "readOnly")

  val Properties = Field(Array(PropertyShapeModel), Shacl + "property")

  val Dependencies = Field(Array(PropertyDependenciesModel), Shapes + "dependencies")

  override def fields: List[Field] =
    List(MinProperties,
         MaxProperties,
         Closed,
         Discriminator,
         DiscriminatorValue,
         ReadOnly,
         Properties,
         Dependencies) ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "NodeShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = NodeShape()
}
