package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.XMLSerializer
import amf.core.vocabulary.Namespace.Shapes
import amf.core.vocabulary.ValueType

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

  override def modelInstance = XMLSerializer()
}
