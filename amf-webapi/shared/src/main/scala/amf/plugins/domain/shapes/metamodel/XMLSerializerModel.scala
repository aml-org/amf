package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.plugins.domain.shapes.models.XMLSerializer
import amf.core.vocabulary.Namespace.Shapes
import amf.core.vocabulary.ValueType

/**
  * Scalar shape metamodel
  */
object XMLSerializerModel extends DomainElementModel {

  val Attribute =
    Field(Bool, Shapes + "xmlAttribute", ModelDoc(ModelVocabularies.Shapes, "xml attribute", "XML attribute mapping"))

  val Wrapped =
    Field(Bool, Shapes + "xmlWrapped", ModelDoc(ModelVocabularies.Shapes, "xml wrapped", "XML wrapped mapping flag"))

  val Name = Field(Str, Shapes + "xmlName", ModelDoc(ModelVocabularies.Shapes, "xml name", "XML name mapping"))

  val Namespace =
    Field(Str, Shapes + "xmlNamespace", ModelDoc(ModelVocabularies.Shapes, "xml namespace", "XML namespace mapping"))

  val Prefix = Field(Str, Shapes + "xmlPrefix", ModelDoc(ModelVocabularies.Shapes, "xml prefix", "XML prefix mapping"))

  override def fields: List[Field] = List(Attribute, Wrapped, Name, Namespace, Prefix) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "XMLSerializer") ++ DomainElementModel.`type`

  override def modelInstance = XMLSerializer()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "XML Serializer",
    "Information about how to encode into XML a particular data shape"
  )
}
