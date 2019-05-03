package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{Document, Http, Hydra}
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
    Http + "paramName",
    ModelDoc(ModelVocabularies.Http, "param name", "Name of a parameter", Seq((Namespace.Schema + "name").iri())))

  val Required =
    Field(Bool, Hydra + "required", ModelDoc(ModelVocabularies.Http, "required", "Marks the parameter as required"))

  val Deprecated = Field(Bool,
                         Document + "deprecated",
                         ModelDoc(ModelVocabularies.Http, "deprecated", "Marks the parameter as deprecated"))

  val AllowEmptyValue = Field(
    Bool,
    Http + "allowEmptyValue",
    ModelDoc(ModelVocabularies.Http, "allow empty value", "Parameter can be passed without value"))

  val Style = Field(Str,
                    Http + "style",
                    ModelDoc(ModelVocabularies.Http, "style", "Encoding style for the parameter information"))

  val Explode = Field(Bool, Http + "explode", ModelDoc(ModelVocabularies.Http, "explode", ""))

  val AllowReserved = Field(Bool, Http + "allowReserved", ModelDoc(ModelVocabularies.Http, "allow reserved", ""))

  val Binding = Field(
    Str,
    Http + "binding",
    ModelDoc(ModelVocabularies.Http,
             "binding",
             "Part of the Request model where the parameter can be encoded (header, path, query param, etc.)")
  )

  val Schema = Field(ShapeModel,
                     Http + "schema",
                     ModelDoc(ModelVocabularies.Http, "schema", "Schema the parameter value must validate"))

  val Payloads = Field(Array(PayloadModel), Http + "payload", ModelDoc(ModelVocabularies.Http, "payload", ""))

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

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Parameter",
    "Piece of data required or returned by an Operation"
  )
}
