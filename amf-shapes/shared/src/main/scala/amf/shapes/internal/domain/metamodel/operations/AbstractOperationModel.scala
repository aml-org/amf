package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait AbstractOperationModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField {

  val Method: Field = Field(Str,
                            Core + "method",
                            ModelDoc(ModelVocabularies.Core, "method", "Type of operation. It follows HTTP semantics"))

  val Request: Field = Field(
    Array(AbstractRequestModel),
    Core + "expects",
    ModelDoc(ModelVocabularies.Core, "expects", "Request information required by the operation"))

  val Responses: Field = Field(Array(AbstractResponseModel),
    Core + "returns",
    ModelDoc(ModelVocabularies.Core, "returns", "Response data returned by the operation"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Name,
    Description,
    Request,
    Responses,
  ) ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "AbstractOperation",
    "Action that can be executed over the data of a particular shape"
  )
}

object AbstractOperationModel extends AbstractOperationModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractOperationModel is an abstract class")

}
