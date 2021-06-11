package amf.plugins.domain.apicontract.models.templates

import amf.core.client.scala.model.domain.templates.ParametrizedDeclaration
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.templates.ParametrizedResourceTypeModel
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
