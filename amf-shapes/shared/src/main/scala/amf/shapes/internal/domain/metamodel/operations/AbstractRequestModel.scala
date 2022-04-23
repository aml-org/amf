package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait AbstractRequestModel extends DomainElementModel with KeyField with NameFieldSchema {

  val QueryParameters: Field = Field(
    Array(AbstractParameterModel),
    Core + "parameter",
    ModelDoc(ModelVocabularies.Core, "parameter", "Parameters associated to the communication model")
  )

  override val `type`: List[ValueType] = List(Core + "Request") ++ DomainElementModel.`type`

  override def fields: List[Field] =
    List(QueryParameters)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "AbstractRequest",
    "Request information for an operation"
  )
  override val key: Field = Name
}

object AbstractRequestModel extends AbstractRequestModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractRequestModel is an abstract class")
}
