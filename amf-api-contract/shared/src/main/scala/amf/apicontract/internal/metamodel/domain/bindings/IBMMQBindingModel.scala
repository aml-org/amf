package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQMessageBinding
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Int, Str}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object IBMMQMessageBindingModel extends MessageBindingModel with BindingVersion {
  val MessageType: Field =
    Field(
      Str,
      ApiBinding + "messageType",
      ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the message")
    )

  val Headers: Field =
    Field(
      Str,
      ApiBinding + "headers",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "headers",
        "Defines the IBM MQ message headers to include with this message. More than one header can be specified as a comma separated list."
      )
    )

  val Description: Field =
    Field(
      Str,
      ApiBinding + "description",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "description",
        "Provides additional information for application developers: describes the message type or format."
      )
    )

  val Expiry: Field =
    Field(
      Int,
      ApiBinding + "expiry",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "expiry",
        "This is a period of time expressed in milliseconds and set by the application that puts the message."
      )
    )

  override def modelInstance: AmfObject = IBMMQMessageBinding()

  override def fields: List[Field] =
    List(MessageType, Headers, Description, Expiry, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQMessageBinding")
}
