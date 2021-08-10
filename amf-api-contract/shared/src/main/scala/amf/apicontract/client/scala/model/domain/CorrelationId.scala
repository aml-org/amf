package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.CorrelationIdModel
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.YMap

class CorrelationId(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable {

  def description: StrField = fields.field(CorrelationIdModel.Description)
  def idLocation: StrField  = fields.field(CorrelationIdModel.Location)

  def withDescription(description: String): this.type = set(CorrelationIdModel.Description, description)
  def withIdLocation(idLocation: String): this.type   = set(CorrelationIdModel.Location, idLocation)

  override def meta: CorrelationIdModel.type    = CorrelationIdModel
  private[amf] override def componentId: String = "/correlation-id/" + name.option().getOrElse("default-id")

  override def nameField: Field = CorrelationIdModel.Name

  override def linkCopy(): CorrelationId = CorrelationId().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = CorrelationId.apply
}

object CorrelationId {

  def apply(): CorrelationId = apply(Annotations())

  def apply(ast: YMap): CorrelationId = apply(Annotations(ast))

  def apply(annotations: Annotations): CorrelationId = new CorrelationId(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): CorrelationId = new CorrelationId(fields, annotations)
}
