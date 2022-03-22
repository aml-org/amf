package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Document, Shapes}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractParameterModel
import amf.shapes.internal.domain.metamodel.common.ExamplesField

/**
  * Parameter metaModel.
  */
object ParameterModel extends AbstractParameterModel with OptionalField with ExamplesField {

  val Deprecated = Field(Bool,
                         Document + "deprecated",
                         ModelDoc(ModelVocabularies.ApiContract, "deprecated", "Marks the parameter as deprecated"))

  val AllowEmptyValue = Field(
    Bool,
    ApiContract + "allowEmptyValue",
    ModelDoc(ModelVocabularies.ApiContract, "allowEmptyValue", "Parameter can be passed without value"))

  val Style = Field(Str,
                    ApiContract + "style",
                    ModelDoc(ModelVocabularies.ApiContract, "style", "Encoding style for the parameter information"))

  val Explode = Field(Bool, ApiContract + "explode", ModelDoc(ModelVocabularies.ApiContract, "explode", ""))

  val AllowReserved =
    Field(Bool, ApiContract + "allowReserved", ModelDoc(ModelVocabularies.ApiContract, "allowReserved", ""))

  val Payloads =
    Field(Array(PayloadModel), ApiContract + "payload", ModelDoc(ModelVocabularies.ApiContract, "payload", ""))

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
