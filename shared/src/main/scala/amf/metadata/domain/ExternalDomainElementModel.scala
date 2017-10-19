package amf.metadata.domain

import amf.metadata.{Field}
import amf.metadata.Type.Str
import amf.vocabulary.Namespace._
import amf.vocabulary.ValueType

object ExternalDomainElementModel extends DomainElementModel {

  val Raw = Field(Str, Document + "raw")

  override def fields: List[Field] = List(Raw)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`
}
