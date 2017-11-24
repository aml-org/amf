package amf.plugins.domain.shapes.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Bool, Str}
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.XMLSerializer
import amf.framework.vocabulary.Namespace.Shapes
import amf.framework.vocabulary.ValueType

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
