package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain._
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

  val Template = Field(Str,
                       Hydra + "template",
                       ModelDoc(ExternalModelVocabularies.Hydra, "template", "URL template for a templated link"))

  val OperationId = Field(Str,
                          Http + "operationId",
                          ModelDoc(ModelVocabularies.Http, "operation ID", "Identifier of the target operation"))

  val Mapping = Field(Array(IriTemplateMappingModel),
                      Hydra + "mapping",
                      ModelDoc(ExternalModelVocabularies.Hydra, "mapping", "Variable mapping for the URL template"))

  val RequestBody = Field(Str, Http + "requestBody", ModelDoc(ModelVocabularies.Http, "request body", ""))

  val Server = Field(ServerModel, Http + "server", ModelDoc(ModelVocabularies.Http, "server", ""))

  override val `type`: List[ValueType] = Hydra + "TemplatedLink" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Template :: OperationId :: Mapping :: RequestBody :: Description :: Server :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = TemplatedLink()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Templated Link",
    "Templated link containing URL template and variables mapping"
  )
}
