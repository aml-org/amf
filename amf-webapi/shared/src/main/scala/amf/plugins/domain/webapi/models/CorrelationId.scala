package amf.plugins.domain.webapi.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel

class CorrelationId(override val fields: Fields, override val annotations: Annotations) extends DomainElement {
  def description: StrField = fields.field(CorrelationIdModel.Description)
  def idLocation: StrField  = fields.field(CorrelationIdModel.Location)

  def withDescription(description: String): this.type = set(CorrelationIdModel.Description, description)
  def withIdLocation(idLocation: String): this.type   = set(CorrelationIdModel.Location, idLocation)

  override def meta: Obj           = CorrelationIdModel
  override def componentId: String = "/default-id"
}
