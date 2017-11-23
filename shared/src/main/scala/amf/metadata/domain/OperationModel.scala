package amf.metadata.domain

import amf.domain.Operation
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type._
import amf.plugins.domain.webapi.metamodel.CreativeWorkModel
import amf.vocabulary.Namespace.{Document, Http, Hydra, Schema}
import amf.vocabulary.{Namespace, ValueType}

/**
  * Operation meta model.
  */
object OperationModel extends DomainElementModel with KeyField with OptionalField {

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

  val Security = Field(Array(DomainElementModel), Namespace.Security + "security")

  val Optional = Field(Bool, Namespace.Http + "optional")

  override val optional: Field = Optional

  override val key: Field = Method

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

  override def modelInstance = Operation()
}
