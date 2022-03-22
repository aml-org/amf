package amf.shapes.internal.domain.metamodel.`abstract`

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.AbstractRequest

trait AbstractRequestModel extends DomainElementModel with KeyField with NameFieldSchema {

  val QueryParameters = Field(
    Array(AbstractParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.Core, "parameter", "Parameters associated to the communication model")
  )

  override val `type`: List[ValueType] = List(Core + "Request") ++ DomainElementModel.`type`

  override def fields: List[Field] =
    List(QueryParameters)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "Request",
    "Request information for an operation"
  )
  override val key: Field = Name
}

object AbstractRequestModel extends AbstractRequestModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractRequestModel is an abstract class")
}
