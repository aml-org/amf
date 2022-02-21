package amf.apiinstance.internal.metamodel.domain

import amf.apiinstance.client.scala.model.domain.FilterRule
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, ApiInstance, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object FilterRuleModel extends DomainElementModel {
  override def modelInstance: AmfObject = FilterRule()

  val DestinationPort: Field = Field(Array(Str),
    ApiInstance + "destinationNetworkPort",
    ModelDoc(ModelVocabularies.ApiInstance, "destinationNetworkPort", "Network port used as the destination of a network request"))

  val DestinationIPAddress: Field = Field(Array(Str),
    ApiInstance + "destinationIPAddress",
    ModelDoc(ModelVocabularies.ApiInstance, "destinationIPAddress", "IP ranges in CIDR notation that are allowed as the destination of a network request"))

  val SourcePort: Field = Field(Array(Str),
    ApiInstance + "sourceNetworkPort",
    ModelDoc(ModelVocabularies.ApiInstance, "sourceNetworkPort", "Network port used as the source of a network request"))

  val SourceIpAddress: Field = Field(Array(Str),
    ApiInstance + "sourceIPAddress",
    ModelDoc(ModelVocabularies.ApiInstance, "sourceIPAddress", "IP ranges in CIDR notation that are allowed as the source of a network request"))

  val DirectSourceIpAddress: Field = Field(Array(Str),
    ApiInstance + "directSourceIPAddress",
    ModelDoc(ModelVocabularies.ApiInstance, "directSourceIPAddress", "IP ranges in CIDR notation that are allowed as the source of a network request"))

  val Host: Field = Field(Array(Str),
    ApiInstance + "host",
    ModelDoc(ModelVocabularies.ApiInstance, "host", "host targeting the network request"))

  val Protocol: Field = Field(Array(Str),
    ApiInstance + "networkProtocol",
    ModelDoc(ModelVocabularies.ApiInstance, "protocol", "transport protocol used for the request"))

  val SourceType: Field = Field(Str,
    ApiInstance + "sourceType",
    ModelDoc(ModelVocabularies.ApiInstance, "sourceType", "type of sources allowed (local or external networks)"))

  val Method = Field(Array(Str),
    ApiContract + "method",
    ModelDoc(ModelVocabularies.ApiContract, "method", "HTTP method required to invoke the operation"))

  val Path =
    Field(Array(Str), ApiContract + "path", ModelDoc(ModelVocabularies.ApiContract, "path", "Path template for an endpoint"))

  val Header =
    Field(Array(Str), ApiContract + "header", ModelDoc(ModelVocabularies.ApiContract, "header", ""))

  val Enabled: Field = Field(Array(Bool),
    Core + "enabled",
    ModelDoc(ModelVocabularies.ApiInstance, "enabled", "Marks this rule as enabled"))


  override val `type`: List[ValueType] = ApiInstance + "FilterRule" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    DestinationPort,
    DestinationIPAddress,
    SourcePort,
    SourceIpAddress,
    DirectSourceIpAddress,
    Host,
    Protocol,
    SourceType,
    Method,
    Path
  ) ++ DomainElementModel.fields
}
