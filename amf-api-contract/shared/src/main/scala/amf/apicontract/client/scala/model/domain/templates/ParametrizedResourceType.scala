package amf.apicontract.client.scala.model.domain.templates

import amf.core.client.scala.model.domain.templates.ParametrizedDeclaration
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.templates.ParametrizedResourceTypeModel
import org.yaml.model.YPart

case class ParametrizedResourceType(fields: Fields, annotations: Annotations)
    extends ParametrizedDeclaration(fields, annotations) {
  override def meta = ParametrizedResourceTypeModel
}

object ParametrizedResourceType {
  def apply(): ParametrizedResourceType = apply(Annotations())

  def apply(ast: YPart): ParametrizedResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedResourceType = ParametrizedResourceType(Fields(), annotations)
}
