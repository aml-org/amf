package amf.shapes.internal.domain.metamodel.`abstract`

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{AmfCore, Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.AbstractOperation
import amf.core.internal.metamodel.Type.Array

// TODO: unify with api model:
// Option 1: move part of Operation model to a generic common operation (with request, response, parameters) to core. ApiOperation on api-contract to inherits of that
// Option 2: move all model to amf-model new package. amf-model will contain all objects (including shapes? at least any shape will be needed) That module can be before amf-shapes or event before amf-aml solving the problem of json schema.

trait AbstractOperationModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField  {

  val Method: Field = Field(Str,
                      Core + "method",
                     ModelDoc(ModelVocabularies.Core, "method", "Type of operation. It follows HTTP semantics"))

  val Request: Field = Field(Array(AbstractRequestModel),
    Core + "expects",
    ModelDoc(ModelVocabularies.Core, "expects", "Request information required by the operation"))

  val Response: Field = Field(AbstractResponseModel,
                        Core + "returns",
                        ModelDoc(ModelVocabularies.Core, "returns", "Response data returned by the operation"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Name,
    Description,
    Request,
    Response,
  ) ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "Operation",
    "Action that can be executed over the data of a particular shape"
  )
}

object AbstractOperationModel extends AbstractOperationModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractOperationModel is an abstract class")

}
