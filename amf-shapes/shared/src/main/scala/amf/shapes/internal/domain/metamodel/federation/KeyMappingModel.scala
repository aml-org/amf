package amf.shapes.internal.domain.metamodel.federation

import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.federation.KeyMapping

trait KeyMappingModel extends DomainElementModel {

  override def modelInstance: KeyMapping

  val Source: Field

  val Target: Field

  override val `type`: List[ValueType] = Federation + "KeyMapping" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "KeyMapping",
    "Model that indicates how other elements map to a federation Key's components"
  )
}