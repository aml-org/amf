package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.{Document, Http, Hydra}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.ExampleField
import amf.plugins.domain.webapi.models.Parameter

/**
  * Parameter metaModel.
  */
object ParameterModel
    extends DomainElementModel
    with LinkableElementModel
    with KeyField
    with NameFieldSchema
    with OptionalField
    with ExampleField
    with DescriptionField {

  val ParameterName = Field(Str, Http + "paramName")

  val Required = Field(Bool, Hydra + "required")

  val Deprecated = Field(Bool, Document + "deprecated")

  val AllowEmptyValue = Field(Bool, Http + "allowEmptyValue")

  val Style = Field(Str, Http + "style")

  val Explode = Field(Bool, Http + "explode")

  val AllowReserved = Field(Bool, Http + "allowReserved")

  val Binding = Field(Str, Http + "binding")

  val Schema = Field(ShapeModel, Http + "schema")

  val Payloads = Field(Array(PayloadModel), Http + "payload")

  override val key: Field = Name

  override val `type`: List[ValueType] = Http + "Parameter" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Name,
         ParameterName,
         Description,
         Required,
         Deprecated,
         AllowEmptyValue,
         Style,
         Explode,
         AllowReserved,
         Binding,
         Schema,
         Payloads,
         Examples) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = Parameter()
}
