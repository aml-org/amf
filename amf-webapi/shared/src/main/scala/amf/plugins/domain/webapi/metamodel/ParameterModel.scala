package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema => SchemaNamespace}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.webapi.models.Parameter

/**
  * Parameter metamodel.
  */
object ParameterModel extends DomainElementModel with LinkableElementModel with KeyField with OptionalField {

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
