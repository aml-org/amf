package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Core
import amf.core.vocabulary.ValueType

object CorrelationIdModel extends DomainElementModel with DescriptionField {

  val Location = Field(
    Str,
    Core + "location",
    ModelDoc(ModelVocabularies.Core, "location", "structural location of the information where the id is located"))

  override val `type`: List[ValueType] = Core + "CorrelationId" :: DomainElementModel.`type`

  override val fields: List[Field] = Description :: Location :: DomainElementModel.fields

  override def modelInstance = ???

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "CorrelationId",
    "Model defining an identifier that can used for message tracing and correlation"
  )
}
