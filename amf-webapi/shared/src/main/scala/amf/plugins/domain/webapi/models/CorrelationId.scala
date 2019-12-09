package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import org.yaml.model.YMap

class CorrelationId(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable {

  def description: StrField = fields.field(CorrelationIdModel.Description)
  def idLocation: StrField  = fields.field(CorrelationIdModel.Location)

  def withDescription(description: String): this.type = set(CorrelationIdModel.Description, description)
  def withIdLocation(idLocation: String): this.type   = set(CorrelationIdModel.Location, idLocation)

  override def meta: Obj           = CorrelationIdModel
  override def componentId: String = "/default-id"

  override protected def nameField: Field = CorrelationIdModel.Name

  override def linkCopy(): CorrelationId = CorrelationId().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = CorrelationId.apply
}

object CorrelationId {

  def apply(): CorrelationId = apply(Annotations())

  def apply(ast: YMap): CorrelationId = apply(Annotations(ast))

  def apply(annotations: Annotations): CorrelationId = new CorrelationId(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): CorrelationId = new CorrelationId(fields, annotations)
}
