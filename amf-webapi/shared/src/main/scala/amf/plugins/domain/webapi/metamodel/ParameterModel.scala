package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.vocabulary.Namespace.{ApiContract, Document}
import amf.core.vocabulary.{Namespace, ValueType}
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

  val ParameterName = Field(
    Str,
    ApiContract + "paramName",
    ModelDoc(ModelVocabularies.ApiContract, "param name", "Name of a parameter", Seq((Namespace.Core + "name").iri())))

  val Required =
    Field(Bool, ApiContract + "required", ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required"))

  val Deprecated = Field(Bool,
                         Document + "deprecated",
                         ModelDoc(ModelVocabularies.ApiContract, "deprecated", "Marks the parameter as deprecated"))

  val AllowEmptyValue = Field(
    Bool,
    ApiContract + "allowEmptyValue",
    ModelDoc(ModelVocabularies.ApiContract, "allow empty value", "Parameter can be passed without value"))

  val Style = Field(Str,
                    ApiContract + "style",
                    ModelDoc(ModelVocabularies.ApiContract, "style", "Encoding style for the parameter information"))

  val Explode = Field(Bool, ApiContract + "explode", ModelDoc(ModelVocabularies.ApiContract, "explode", ""))

  val AllowReserved = Field(Bool, ApiContract + "allowReserved", ModelDoc(ModelVocabularies.ApiContract, "allow reserved", ""))

  val Binding = Field(
    Str,
    ApiContract + "binding",
    ModelDoc(ModelVocabularies.ApiContract,
             "binding",
             "Part of the Request model where the parameter can be encoded (header, path, query param, etc.)")
  )

  val Schema = Field(ShapeModel,
                     ApiContract + "schema",
                     ModelDoc(ModelVocabularies.ApiContract, "schema", "Schema the parameter value must validate"))

  val Payloads = Field(Array(PayloadModel), ApiContract + "payload", ModelDoc(ModelVocabularies.ApiContract, "payload", ""))

  override val key: Field = Name

  override val `type`: List[ValueType] = ApiContract + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] =
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

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Parameter",
    "Piece of data required or returned by an Operation"
  )
}
