package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Bool, Str}
import amf.metadata.domain.{DomainElementModel, KeyField, LinkableElementModel}
import amf.metadata.shape.ShapeModel
import amf.plugins.domain.webapi.models.Parameter
import amf.vocabulary.Namespace.{Http, Hydra, Schema => SchemaNamespace}
import amf.vocabulary.ValueType

/**
  * Parameter metamodel.
  */
object ParameterModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name = Field(Str, SchemaNamespace + "name")

  val Description = Field(Str, SchemaNamespace + "description")

  val Required = Field(Bool, Hydra + "required")

  val Binding = Field(Str, Http + "binding")

  val Schema = Field(ShapeModel, Http + "schema")

  override val key: Field = Name

  override val `type`: List[ValueType] = Http + "Parameter" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name, Description, Required, Binding, Schema) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = Parameter()
}
