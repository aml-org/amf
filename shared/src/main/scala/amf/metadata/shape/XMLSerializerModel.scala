package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Bool, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Shapes
import amf.vocabulary.ValueType

/**
  * Scalar shape metamodel
  */
object XMLSerializerModel extends DomainElementModel {

  val Attribute = Field(Bool, Shapes + "xmlAttribute")

  val Wrapped = Field(Bool, Shapes + "xmlWrapped")

  val Name = Field(Str, Shapes + "xmlName")

  val Namespace = Field(Str, Shapes + "xmlNamespace")

  val Prefix = Field(Str, Shapes + "xmlPrefix")

  override def fields: List[Field] = List(Attribute, Wrapped, Name, Namespace, Prefix) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "XMLSerializer") ++ DomainElementModel.`type`
}
