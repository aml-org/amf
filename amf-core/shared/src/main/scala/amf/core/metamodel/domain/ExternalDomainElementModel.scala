package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.model.domain.ExternalDomainElement
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

object ExternalDomainElementModel extends DomainElementModel {

  val Raw = Field(Str, Document + "raw")

  val MediaType = Field(Str, Http + "mediaType")

  override def fields: List[Field] = List(Raw, MediaType)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`

  override def modelInstance = ExternalDomainElement()
}
