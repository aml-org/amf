package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.ExternalDomainElement
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType

object ExternalDomainElementModel extends DomainElementModel {

  val Raw = Field(Str, Document + "raw")

  override def fields: List[Field] = List(Raw)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`

  override def modelInstance = ExternalDomainElement()
}
