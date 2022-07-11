package amf.apicontract.internal.metamodel.domain.federation

import amf.apicontract.client.scala.model.domain.federation.ParameterKeyMapping
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.extensions.PropertyShapePathModel
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.federation.KeyMappingModel

object ParameterKeyMappingModel extends KeyMappingModel {

  override val `type`: List[ValueType] =
    Federation + "ParameterKeyMapping" :: Federation + "KeyMapping" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "ParameterKeyMapping",
    "Model that indicates how other elements map to a federation Key's components"
  )

  val Source: Field = Field(
    ParameterModel,
    Federation + "mappingSource",
    ModelDoc(ModelVocabularies.Federation, "mappingSource", "Parameter to use as source for this mapping")
  )

  val Target: Field = Field(
    PropertyShapePathModel,
    Federation + "mappingTarget",
    ModelDoc(ModelVocabularies.Federation, "mappingTarget", "Path to target Property Shape of this mapping")
  )

  override val fields: List[Field] = Source :: Target :: DomainElementModel.fields

  override def modelInstance: ParameterKeyMapping = ParameterKeyMapping()
}
