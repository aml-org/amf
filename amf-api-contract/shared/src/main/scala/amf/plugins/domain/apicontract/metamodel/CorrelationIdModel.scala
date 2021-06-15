package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.plugins.domain.apicontract.models.CorrelationId

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
