package amf.metadata.domain

import amf.domain.ExternalDomainElement
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.vocabulary.Namespace._
import amf.vocabulary.ValueType

object ExternalDomainElementModel extends DomainElementModel {

  val Raw = Field(Str, Document + "raw")

  override def fields: List[Field] = List(Raw)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`

  override def modelInstance = ExternalDomainElement()
}
