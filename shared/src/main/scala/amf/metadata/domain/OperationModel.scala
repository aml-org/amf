package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type._
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel
import amf.vocabulary.Namespace.{Document, Http, Hydra, Schema}
import amf.vocabulary.{Namespace, ValueType}

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

  val Accepts = Field(Array(Str), Http + "accepts")

  val ContentType = Field(Array(Str), Http + "contentType")

  val Request = Field(RequestModel, Hydra + "expects")

  val Responses = Field(Array(ResponseModel), Hydra + "returns")

  val Security = Field(Array(ParametrizedSecuritySchemeModel), Namespace.Security + "security")

  override val `type`: List[ValueType] = Hydra + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(Method,
                                          Name,
                                          Description,
                                          Deprecated,
                                          Summary,
                                          Documentation,
                                          Schemes,
                                          Accepts,
                                          ContentType,
                                          Request,
                                          Responses,
                                          Security) ++ DomainElementModel.fields
}
