package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Core
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.CorrelationId

object CorrelationIdModel
    extends DomainElementModel
    with NameFieldSchema
    with DescriptionField
    with LinkableElementModel {

  val Location = Field(Str,
                       Core + "location",
                       ModelDoc(ModelVocabularies.Core, "location", "Structural location of a piece of information"))

  override val `type`: List[ValueType] = Core + "CorrelationId" :: DomainElementModel.`type`

  override val fields
    : List[Field] = Name :: Description :: Location :: LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = CorrelationId()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "CorrelationId",
    "Model defining an identifier that can used for message tracing and correlation"
  )
}
