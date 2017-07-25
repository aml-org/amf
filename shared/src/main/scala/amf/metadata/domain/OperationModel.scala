package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type._
import amf.vocabulary.Namespace.{Document, Http, Hydra, Schema}
import amf.vocabulary.ValueType

/**
  * Operation metamodel
  */
object OperationModel extends DomainElementModel {

  val Method = Field(Str, Hydra + "method")

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Deprecated = Field(Bool, Document + "deprecated")

  val Summary = Field(Str, Http + "guiSummary")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val Schemes = Field(Array(Str), Http + "scheme")

  val Request = Field(RequestModel, Hydra + "expects")

  val Responses = Field(Array(ResponseModel), Hydra + "returns")

  override val `type`: List[ValueType] = Hydra + "Operation" :: DomainElementModel.`type`

  override val fields
    : List[Field] = Method :: Name :: Description :: Deprecated :: Summary :: Documentation :: Schemes :: Request :: Responses :: DomainElementModel.fields
}
