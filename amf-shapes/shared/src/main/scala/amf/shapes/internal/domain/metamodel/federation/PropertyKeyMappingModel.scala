package amf.shapes.internal.domain.metamodel.federation

import amf.shapes.client.scala.model.domain.federation.PropertyKeyMapping
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object PropertyKeyMappingModel extends KeyMappingModel {

  override val `type`: List[ValueType] = Federation + "PropertyKeyMapping" :: Federation + "KeyMapping" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "PropertyKeyMapping",
    "Model that indicates how other elements map to a federation Key's components"
  )

  override val Source: Field = Field(
    PropertyShapeModel,
    Federation + "mappingSource",
    ModelDoc(ModelVocabularies.Federation, "mappingSource", "Property Shape to use as source for this mapping")
  )

  override val Target: Field = Field(
    Str,
    Federation + "mappingTarget",
    ModelDoc(ModelVocabularies.Federation, "mappingTarget", "Name of external target Property Shape of this mapping")
  )

  override val fields: List[Field] = Source :: Target :: DomainElementModel.fields

  override def modelInstance: PropertyKeyMapping = PropertyKeyMapping()
}
