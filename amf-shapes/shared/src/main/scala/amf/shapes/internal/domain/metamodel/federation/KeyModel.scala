package amf.shapes.internal.domain.metamodel.federation

import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool}
import amf.core.internal.metamodel.domain.extensions.PropertyShapePathModel
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.federation.Key

object KeyModel extends DomainElementModel {

  val Components: Field =
    Field(
      Array(PropertyShapePathModel),
      Federation + "keyComponents",
      ModelDoc(ModelVocabularies.Federation, "keyComponents", "Components that make up this Key")
    )

  val IsResolvable: Field =
    Field(
      Bool,
      Federation + "isResolvable",
      ModelDoc(ModelVocabularies.Federation, "isResolvable"),
      defaultValue = Some(true)
    )

  override def modelInstance: Key = Key()

  override val `type`: List[ValueType] = Federation + "Key" :: DomainElementModel.`type`

  override val fields: List[Field] = Components :: IsResolvable :: DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "Key",
    "Model that represents the Key of an element to be retrieved by the federated graph"
  )
}
