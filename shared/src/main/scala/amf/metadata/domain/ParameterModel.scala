package amf.metadata.domain
import amf.metadata.Field
import amf.metadata.Type.{Bool, Str}
import amf.vocabulary.Namespace.{Http, Hydra, Schema => SchemaNamespace}
import amf.vocabulary.ValueType

/**
  * Parameter metamodel.
  */
object ParameterModel extends DomainElementModel {

  val Name = Field(Str, SchemaNamespace + "name")

  val Description = Field(Str, SchemaNamespace + "description")

  val Required = Field(Bool, Hydra + "required")

  val Binding = Field(Str, Http + "binding")

  val Schema = Field(Str, Http + "schema")

  override val `type`: List[ValueType] = Http + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] = Name :: Description :: Required :: Binding :: Schema :: DomainElementModel.fields
}
