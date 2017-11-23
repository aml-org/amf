package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type._
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel
import amf.metadata.domain.{DomainElementModel, KeyField}
import amf.plugins.domain.webapi.models.EndPoint
import amf.vocabulary.Namespace.{Http, Hydra, Schema}
import amf.vocabulary.{Namespace, ValueType}

/**
  * EndPoint metamodel
  */
object EndPointModel extends DomainElementModel with KeyField {

  val Path = Field(RegExp, Http + "path")

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Operations = Field(Array(OperationModel), Hydra + "supportedOperation")

  val UriParameters = Field(Array(ParameterModel), Http + "parameter")

  val Security = Field(Array(ParametrizedSecuritySchemeModel), Namespace.Security + "security")

  override val key: Field = Path

  override val `type`: List[ValueType] = Http + "EndPoint" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Path, Name, Description, Operations, UriParameters, Security) ++ DomainElementModel.fields

  override def modelInstance = EndPoint()
}
