package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Document, Shapes}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.templates.OptionalField
import amf.shapes.internal.domain.metamodel.common.ExamplesField
import amf.shapes.internal.domain.metamodel.operations.AbstractParameterModel

/** Parameter metaModel.
  */
object ParameterModel extends AbstractParameterModel with OptionalField with ExamplesField {

  override val ParameterName: Field = Field(
    Str,
    ApiContract + "paramName",
    ModelDoc(ModelVocabularies.ApiContract, "paramName", "Name of a parameter", Seq((Namespace.Core + "name").iri()))
  )

  override val Required: Field =
    Field(
      Bool,
      ApiContract + "required",
      ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required")
    )

  val Deprecated: Field = Field(
    Bool,
    Document + "deprecated",
    ModelDoc(ModelVocabularies.ApiContract, "deprecated", "Marks the parameter as deprecated")
  )

  val AllowEmptyValue: Field = Field(
    Bool,
    ApiContract + "allowEmptyValue",
    ModelDoc(ModelVocabularies.ApiContract, "allowEmptyValue", "Parameter can be passed without value")
  )

  val Style: Field = Field(
    Str,
    ApiContract + "style",
    ModelDoc(ModelVocabularies.ApiContract, "style", "Encoding style for the parameter information")
  )

  val Explode: Field = Field(Bool, ApiContract + "explode", ModelDoc(ModelVocabularies.ApiContract, "explode", ""))

  val AllowReserved: Field =
    Field(Bool, ApiContract + "allowReserved", ModelDoc(ModelVocabularies.ApiContract, "allowReserved", ""))

  override val Binding: Field = Field(
    Str,
    ApiContract + "binding",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "binding",
      "Part of the Request model where the parameter can be encoded (header, path, query param, etc.)"
    )
  )

  val Payloads: Field =
    Field(Array(PayloadModel), ApiContract + "payload", ModelDoc(ModelVocabularies.ApiContract, "payload", ""))

  override val key: Field = Name

  override val `type`: List[ValueType] = ApiContract + "Parameter" :: Core + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(
      Name,
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
      Examples
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance: AmfObject = Parameter()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Parameter",
    "Piece of data required or returned by an Operation"
  )
}
