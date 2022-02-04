package amf.apiinstance.internal.metamodel.domain

import amf.core.client.scala.vocabulary.ValueType
import amf.apiinstance.client.scala.model.domain.ProtocolListener
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiInstance, ApiContract}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object ProtocolListenerModel extends DomainElementModel with NameFieldSchema with DescriptionField {
  override def modelInstance: AmfObject = ProtocolListener()

  val NetworkAddress: Field = Field(Str,
    ApiInstance + "networkAddress",
    ModelDoc(ModelVocabularies.ApiInstance, "networkAddress", "Network address where a service is listening"))

  val NetworkPort: Field = Field(Str,
    ApiInstance + "networkPort",
    ModelDoc(ModelVocabularies.ApiInstance, "networkPort", "Network port where a service is listening"))

  val NamedNetworkPort: Field = Field(Str,
    ApiInstance + "namedNetworkPort",
    ModelDoc(ModelVocabularies.ApiInstance, "namedNetworkPort", "Well defined network port"))

  val Protocol: Field = Field(
    Str,
    ApiContract + "protocol",
    ModelDoc(ModelVocabularies.ApiContract, "protocol", "The protocol this URL supports for connection"))

  val Pipe: Field = Field(Str,
    ApiInstance + "pipe",
    ModelDoc(ModelVocabularies.ApiInstance, "pipe", "File where the service is listening"))

  val PipeMode: Field = Field(Str,
    ApiInstance + "pipeMode",
    ModelDoc(ModelVocabularies.ApiInstance, "pipeMode", "FS mode for the pipe file"))

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiInstance,
    "ProtocolListener",
    "Protocol listener accepting network traffic for a particular L4 protocol"
  )

  override val `type`: List[ValueType] = ApiInstance + "ProtocolListener" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    Name,
    Description,
    Protocol,
    NetworkAddress,
    NetworkPort,
    NamedNetworkPort,
    Pipe,
    PipeMode
  )
}
