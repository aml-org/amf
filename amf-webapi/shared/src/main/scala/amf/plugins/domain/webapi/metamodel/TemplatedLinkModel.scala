package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.TemplatedLink

/**
  * Link metaModel.
  */
object TemplatedLinkModel
    extends DomainElementModel
    with LinkableElementModel
    with NameFieldSchema
    with DescriptionField {

  val Template = Field(Str, Hydra + "template")

  val OperationId = Field(Str, Http + "operationId")

  val Mapping = Field(Array(IriTemplateMappingModel), Hydra + "mapping")

  val RequestBody = Field(Str, Http + "requestBody")

  val Server = Field(ServerModel, Http + "server")

  override val `type`: List[ValueType] = Hydra + "TemplatedLink" :: DomainElementModel.`type`

  override def fields: List[Field] =
    Name :: Template :: OperationId :: Mapping :: RequestBody :: Description :: Server :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = TemplatedLink()
}
