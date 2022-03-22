package amf.graphql.client.scala.model.domain

import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.graphql.internal.spec.domain.metamodel.operations.QueryPayloadModel
import amf.shapes.client.scala.model.domain.operations.AbstractPayload
import org.yaml.model.YPart

case class QueryPayload(override val fields: Fields, override val annotations: Annotations)
    extends AbstractPayload(fields, annotations) {
  override def linkCopy(): Linkable = QueryPayload().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = QueryPayload.apply

  override def meta: DomainElementModel = QueryPayloadModel
}

object QueryPayload {
  def apply(): QueryPayload = apply(Annotations())

  def apply(ast: YPart): QueryPayload = apply(Annotations(ast))

  def apply(annotations: Annotations): QueryPayload = new QueryPayload(Fields(), annotations)
}
