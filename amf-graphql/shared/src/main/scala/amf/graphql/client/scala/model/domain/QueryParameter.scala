package amf.graphql.client.scala.model.domain

import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.graphql.internal.spec.domain.metamodel.operations.QueryParameterModel
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import org.yaml.model.YPart

case class QueryParameter(override val fields: Fields, override val annotations: Annotations)
    extends AbstractParameter(fields, annotations) {
  override protected def buildParameter(ann: Annotations): AbstractParameter = QueryParameter()

  override def meta: DomainElementModel = QueryParameterModel
}

object QueryParameter {
  def apply(): QueryParameter = apply(Annotations())

  def apply(ast: YPart): QueryParameter = apply(Annotations(ast))

  def apply(annotations: Annotations): QueryParameter = new QueryParameter(Fields(), annotations)
}
