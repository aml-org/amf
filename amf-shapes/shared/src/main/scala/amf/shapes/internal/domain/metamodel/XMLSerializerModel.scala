package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.domain.models.XMLSerializer

/**
  * Scalar shape metamodel
  */
object XMLSerializerModel extends DomainElementModel {

  val Attribute =
    Field(Bool, Shapes + "xmlAttribute", ModelDoc(ModelVocabularies.Shapes, "xmlAttribute", "XML attribute mapping"))

  val Wrapped =
    Field(Bool, Shapes + "xmlWrapped", ModelDoc(ModelVocabularies.Shapes, "xmlWrapped", "XML wrapped mapping flag"))

  val Name = Field(Str, Shapes + "xmlName", ModelDoc(ModelVocabularies.Shapes, "xmlName", "XML name mapping"))

  val Namespace =
    Field(Str, Shapes + "xmlNamespace", ModelDoc(ModelVocabularies.Shapes, "xmlNamespace", "XML namespace mapping"))

  val Prefix = Field(Str, Shapes + "xmlPrefix", ModelDoc(ModelVocabularies.Shapes, "xmlPrefix", "XML prefix mapping"))

  override val fields: List[Field] = List(Attribute, Wrapped, Name, Namespace, Prefix) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "XMLSerializer") ++ DomainElementModel.`type`

  override def modelInstance = XMLSerializer()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "XMLSerializer",
    "Information about how to encode into XML a particular data shape"
  )
}
